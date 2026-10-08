package com.rota.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.auth.domain.User;
import com.rota.auth.domain.UserRepository;
import com.rota.common.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cadastro, login e refresh token contra um Postgres real, com as migrations do Flyway.
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "management.tracing.enabled=false",
        "rota.seed.enabled=false",
})
@AutoConfigureMockMvc
class AuthFlowIT {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    UserRepository users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void cadastroDevolveTokensQueDaoAcessoAoPerfil() throws Exception {
        String email = newEmail();
        JsonNode auth = body(register(email, "CUSTOMER").andExpect(status().isCreated()));

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + auth.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void emailDuplicadoEhRecusado() throws Exception {
        String email = newEmail();
        register(email, "CUSTOMER").andExpect(status().isCreated());

        register(email, "CUSTOMER").andExpect(status().isConflict());
    }

    @Test
    void ninguemSeCadastraComoAdmin() throws Exception {
        register(newEmail(), "ADMIN").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void senhaErradaNaoEntra() throws Exception {
        String email = newEmail();
        register(email, "DRIVER");

        send("/auth/login", """
                {"email": "%s", "password": "senha-errada"}""".formatted(email))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenRotacionaEReusoDerrubaTodasAsSessoes() throws Exception {
        String email = newEmail();
        String first = body(register(email, "CUSTOMER")).get("refreshToken").asText();

        // Uso normal: troca o token por um novo par.
        String second = body(refresh(first).andExpect(status().isOk())).get("refreshToken").asText();

        // Alguém reapresenta o token antigo (já revogado): assume-se vazamento.
        refresh(first).andExpect(status().isUnauthorized());

        // Até o token legítimo mais recente deixa de valer; o usuário precisa entrar de novo.
        refresh(second).andExpect(status().isUnauthorized());
        send("/auth/login", """
                {"email": "%s", "password": "senha-forte-123"}""".formatted(email))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRevogaORefreshToken() throws Exception {
        String token = body(register(newEmail(), "RESTAURANT")).get("refreshToken").asText();

        send("/auth/logout", "{\"refreshToken\": \"" + token + "\"}").andExpect(status().is2xxSuccessful());

        refresh(token).andExpect(status().isUnauthorized());
    }

    @Test
    void contaBloqueadaPerdeASessaoENaoEntraMais() throws Exception {
        String admin = adminToken();
        String email = newEmail();
        JsonNode customer = body(register(email, "CUSTOMER"));
        long customerId = customer.at("/user/id").asLong();

        setActive(admin, customerId, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        refresh(customer.get("refreshToken").asText()).andExpect(status().isUnauthorized());
        send("/auth/login", """
                {"email": "%s", "password": "senha-forte-123"}""".formatted(email))
                .andExpect(status().isForbidden());

        setActive(admin, customerId, true).andExpect(status().isOk());
        send("/auth/login", """
                {"email": "%s", "password": "senha-forte-123"}""".formatted(email))
                .andExpect(status().isOk());
    }

    @Test
    void adminFiltraUsuariosPorPerfilEBusca() throws Exception {
        String admin = adminToken();
        String marker = UUID.randomUUID().toString().substring(0, 8);
        register("motorista-" + marker + "@rota.dev", "DRIVER");
        register("cliente-" + marker + "@rota.dev", "CUSTOMER");

        mvc.perform(get("/auth/admin/users").param("role", "DRIVER").param("q", marker)
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("motorista-" + marker + "@rota.dev"));
    }

    @Test
    void soAdminGerenciaUsuariosENaoBloqueiaASiMesmo() throws Exception {
        String customer = "Bearer " + body(register(newEmail(), "CUSTOMER")).get("accessToken").asText();
        mvc.perform(get("/auth/admin/users").header("Authorization", customer))
                .andExpect(status().isForbidden());

        String admin = adminToken();
        long adminId = body(mvc.perform(get("/auth/me").header("Authorization", admin))).get("id").asLong();
        setActive(admin, adminId, false).andExpect(status().isUnprocessableEntity());
    }

    /** Admin não se cadastra pela API: é criado direto no banco, como no seed. */
    private String adminToken() throws Exception {
        String email = newEmail();
        users.save(new User("Admin de Teste", email, passwordEncoder.encode("senha-forte-123"), Role.ADMIN, null));
        JsonNode auth = body(send("/auth/login", """
                {"email": "%s", "password": "senha-forte-123"}""".formatted(email)));
        return "Bearer " + auth.get("accessToken").asText();
    }

    private ResultActions setActive(String token, long userId, boolean active) throws Exception {
        return mvc.perform(patch("/auth/admin/users/" + userId + "/status")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\": " + active + "}"));
    }

    private ResultActions register(String email, String role) throws Exception {
        return send("/auth/register", """
                {"name": "Pessoa de Teste", "email": "%s", "password": "senha-forte-123", "role": "%s"}"""
                .formatted(email, role));
    }

    private ResultActions refresh(String token) throws Exception {
        return send("/auth/refresh", "{\"refreshToken\": \"" + token + "\"}");
    }

    private ResultActions send(String path, String content) throws Exception {
        return mvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private static String newEmail() {
        return "it-" + UUID.randomUUID() + "@rota.dev";
    }
}
