package com.rota.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
