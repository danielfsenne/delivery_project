package com.rota.order.support;

import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.application.port.RestaurantCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

/**
 * Sobe o order-service inteiro contra Postgres, Redis e RabbitMQ reais (Testcontainers).
 * Só as chamadas a outros serviços (catálogo e pagamento) são dublês: o resto é o mesmo
 * código que roda em produção, inclusive Flyway, outbox e publisher confirms.
 *
 * <p>Os containers sobem uma vez por execução e são compartilhados por todas as classes de teste,
 * assim como o contexto do Spring em cache. O Ryuk do Testcontainers os remove no fim.</p>
 */
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "management.tracing.enabled=false",
        "rota.messaging.outbox.poll-interval-ms=100",
})
@AutoConfigureMockMvc
public abstract class IntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @ServiceConnection
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    static {
        POSTGRES.start();
        REDIS.start();
        RABBIT.start();
    }

    @MockitoBean
    protected RestaurantCatalog catalog;

    @MockitoBean
    protected PaymentGateway payments;

    @Autowired
    private JwtService jwt;

    protected String tokenFor(long userId, Role role) {
        return "Bearer " + jwt.generateAccessToken(
                new AuthenticatedUser(userId, role.name().toLowerCase() + userId + "@rota.dev", role));
    }
}
