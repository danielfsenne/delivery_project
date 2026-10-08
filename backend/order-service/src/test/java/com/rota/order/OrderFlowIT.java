package com.rota.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.events.RotaEvents;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.common.security.Role;
import com.rota.order.application.port.PaymentGateway.PaymentOutcome;
import com.rota.order.application.port.RestaurantCatalog.Quote;
import com.rota.order.application.port.RestaurantCatalog.QuoteLine;
import com.rota.order.application.port.RestaurantCatalog.QuotedItem;
import com.rota.order.domain.DeliveryAddress;
import com.rota.order.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pedido de ponta a ponta pela API: carrinho no Redis, checkout com cupom no Postgres,
 * pagamento, avanço pelo restaurante e eventos publicados no RabbitMQ pelo outbox.
 */
class OrderFlowIT extends IntegrationTest {

    private static final long RESTAURANT_ID = 10L;
    private static final long OWNER_ID = 77L;
    private static final String EVENTS_QUEUE = "it.order-events";

    /** Cada teste usa um cliente novo, para não herdar carrinho nem pedidos de outro. */
    private static final AtomicLong CUSTOMERS = new AtomicLong(500);

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    AmqpAdmin amqp;

    @Autowired
    RabbitTemplate rabbit;

    private long customerId;

    @BeforeEach
    void setUp() {
        customerId = CUSTOMERS.incrementAndGet();

        when(catalog.quote(anyLong(), anyList())).thenAnswer(inv -> quote(inv.getArgument(1)));

        // Fila só do teste, ligada ao exchange real, para ver o que o outbox publica.
        Queue queue = new Queue(EVENTS_QUEUE, false, false, false);
        amqp.declareQueue(queue);
        amqp.declareBinding(BindingBuilder.bind(queue).to(new TopicExchange(RotaEvents.EXCHANGE)).with("order.#"));
        amqp.purgeQueue(EVENTS_QUEUE, false);
    }

    @Test
    void pedidoVaiDoCarrinhoAoRestauranteEPublicaCadaMudancaDeStatus() throws Exception {
        jdbc.update("""
                INSERT INTO coupons (code, type, value, min_order_value, usage_limit)
                VALUES ('IT15', 'FIXED', 15, 0, 5)
                """);
        when(payments.charge(any(), any(), any(), any())).thenReturn(new PaymentOutcome(true, null));

        asCustomer(post("/cart/items"), """
                {"restaurantId": %d, "productId": 1, "quantity": 2}""".formatted(RESTAURANT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(60.0));
        asCustomer(put("/cart/coupon"), "{\"code\": \"IT15\"}")
                .andExpect(jsonPath("$.discount").value(15.0));

        JsonNode order = body(asCustomer(post("/orders"), checkoutBody())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(50.0)));
        long orderId = order.get("id").asLong();

        // O carrinho foi esvaziado e o cupom contou um uso.
        asCustomer(get("/cart"), null).andExpect(jsonPath("$.itemCount").value(0));
        assertThat(jdbc.queryForObject("SELECT used_count FROM coupons WHERE code = 'IT15'", Integer.class))
                .isEqualTo(1);

        for (String next : List.of("RESTAURANT_ACCEPTED", "PREPARING", "READY_FOR_PICKUP")) {
            changeStatus(orderId, tokenFor(OWNER_ID, Role.RESTAURANT), next).andExpect(status().isOk());
        }

        // Nem o admin pula etapas: a máquina de estados recusa com 409.
        changeStatus(orderId, tokenFor(1, Role.ADMIN), "DELIVERED")
                .andExpect(status().isConflict());

        // Cada transição ficou registrada no histórico de auditoria.
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM order_history WHERE order_id = ?", Integer.class, orderId))
                .isGreaterThanOrEqualTo(5);

        List<Message> events = receiveUntil(orderId, RotaEvents.ORDER_READY_FOR_PICKUP);
        assertThat(events).extracting(m -> m.getMessageProperties().getReceivedRoutingKey())
                .contains(RotaEvents.ORDER_READY_FOR_PICKUP);
        assertThat(events.stream()
                .filter(m -> m.getMessageProperties().getReceivedRoutingKey().equals(RotaEvents.ORDER_STATUS_CHANGED))
                .map(this::eventStatus))
                .containsExactly("CREATED", "PAYMENT_PENDING", "PAID", "RESTAURANT_ACCEPTED", "PREPARING",
                        "READY_FOR_PICKUP");

        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM outbox_events WHERE published_at IS NULL", Integer.class)).isZero();
    }

    @Test
    void pedidoAguardaPagamentoQuandoOServicoEstaForaEPodeSerPagoDepois() throws Exception {
        when(payments.charge(any(), any(), any(), any()))
                .thenThrow(new ServiceUnavailableException("payment-service fora do ar", null));

        asCustomer(post("/cart/items"), """
                {"restaurantId": %d, "productId": 2, "quantity": 1}""".formatted(RESTAURANT_ID));
        long orderId = body(asCustomer(post("/orders"), checkoutBody())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING")))
                .get("id").asLong();

        // O serviço voltou: o cliente tenta pagar de novo.
        doReturn(new PaymentOutcome(true, null)).when(payments).charge(any(), any(), any(), any());

        asCustomer(post("/orders/" + orderId + "/pay"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void clienteNaoVePedidoDeOutroCliente() throws Exception {
        when(payments.charge(any(), any(), any(), any())).thenReturn(new PaymentOutcome(true, null));
        asCustomer(post("/cart/items"), """
                {"restaurantId": %d, "productId": 3, "quantity": 1}""".formatted(RESTAURANT_ID));
        long orderId = body(asCustomer(post("/orders"), checkoutBody())).get("id").asLong();

        mvc.perform(get("/orders/" + orderId).header("Authorization", tokenFor(9_999, Role.CUSTOMER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminVeTodosOsPedidosEOsNumerosDoDia() throws Exception {
        when(payments.charge(any(), any(), any(), any())).thenReturn(new PaymentOutcome(true, null));
        asCustomer(post("/cart/items"), """
                {"restaurantId": %d, "productId": 4, "quantity": 1}""".formatted(RESTAURANT_ID));
        long orderId = body(asCustomer(post("/orders"), checkoutBody())).get("id").asLong();
        String admin = tokenFor(1, Role.ADMIN);

        JsonNode page = body(mvc.perform(get("/orders/admin").param("status", "PAID").param("size", "100")
                        .header("Authorization", admin))
                .andExpect(status().isOk()));
        assertThat(page.get("content").findValuesAsText("id")).contains(String.valueOf(orderId));

        mvc.perform(get("/orders/admin/stats").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topRestaurants[0].restaurantId").value(RESTAURANT_ID));
        assertThat(body(mvc.perform(get("/orders/admin/stats").header("Authorization", admin)))
                .get("ordersToday").asLong()).isPositive();

        asCustomer(get("/orders/admin/stats"), null).andExpect(status().isForbidden());
    }

    private ResultActions asCustomer(MockHttpServletRequestBuilder request, String content) throws Exception {
        request.header("Authorization", tokenFor(customerId, Role.CUSTOMER));
        if (content != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(content);
        }
        return mvc.perform(request);
    }

    private ResultActions changeStatus(long orderId, String token, String target) throws Exception {
        return mvc.perform(patch("/orders/" + orderId + "/status")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"" + target + "\"}"));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    /**
     * Lê os eventos do pedido até chegar o de {@code lastRoutingKey}. O relay do outbox publica
     * de forma assíncrona, e eventos de pedidos de outros testes podem cair na mesma fila.
     */
    private List<Message> receiveUntil(long orderId, String lastRoutingKey) {
        List<Message> messages = new ArrayList<>();
        Message message;
        while ((message = rabbit.receive(EVENTS_QUEUE, 10_000)) != null) {
            if (payload(message).get("orderId").asLong() != orderId) {
                continue;
            }
            messages.add(message);
            if (message.getMessageProperties().getReceivedRoutingKey().equals(lastRoutingKey)) {
                break;
            }
        }
        return messages;
    }

    private String eventStatus(Message message) {
        return payload(message).get("status").asText();
    }

    private JsonNode payload(Message message) {
        try {
            return json.readTree(message.getBody());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String checkoutBody() {
        return """
                {
                  "paymentMethod": "PIX",
                  "deliveryAddress": {
                    "street": "Rua das Flores", "number": "100", "district": "Centro",
                    "city": "São Paulo", "state": "SP", "zipCode": "01001-000"
                  }
                }""";
    }

    /** Catálogo de mentira: todo produto custa R$ 30, entrega R$ 5 e pedido mínimo R$ 20. */
    private static Quote quote(List<QuoteLine> lines) {
        List<QuotedItem> items = lines.stream()
                .map(l -> new QuotedItem(l.productId(), "Produto " + l.productId(), new BigDecimal("30.00"),
                        l.quantity(), List.of()))
                .toList();
        DeliveryAddress pickup = new DeliveryAddress("Av. Paulista", "1000", null, "Bela Vista", "São Paulo",
                "SP", "01310-100", -23.561, -46.656);
        return new Quote(RESTAURANT_ID, "Restaurante de Teste", OWNER_ID, true, new BigDecimal("5.00"),
                new BigDecimal("20.00"), pickup, items);
    }
}
