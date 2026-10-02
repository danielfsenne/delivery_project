package com.rota.notification.infrastructure.websocket;

import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.JwtProperties;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class StompAuthInterceptorTest {

    private final JwtService jwt = new JwtService(
            new JwtProperties("test-secret-with-at-least-32-bytes-ok", Duration.ofMinutes(15), Duration.ofDays(7)));
    private final StompAuthInterceptor interceptor = new StompAuthInterceptor(jwt);
    private final MessageChannel channel = mock(MessageChannel.class);

    private final AuthenticatedUser customer = new AuthenticatedUser(1L, "c@rota.dev", Role.CUSTOMER);
    private final AuthenticatedUser driver = new AuthenticatedUser(3L, "d@rota.dev", Role.DRIVER);

    @Test
    void connectComTokenValidoDefineOUsuario() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + jwt.generateAccessToken(customer));

        Message<?> result = interceptor.preSend(message(accessor), channel);

        StompHeaderAccessor after = StompHeaderAccessor.wrap(result);
        assertThat(after.getUser()).isNotNull();
        assertThat(after.getUser().getName()).isEqualTo("1");
    }

    @Test
    void connectSemTokenOuComTokenInvalidoEhRecusado() {
        StompHeaderAccessor semToken = StompHeaderAccessor.create(StompCommand.CONNECT);
        assertThatThrownBy(() -> interceptor.preSend(message(semToken), channel))
                .isInstanceOf(MessageDeliveryException.class);

        StompHeaderAccessor invalido = StompHeaderAccessor.create(StompCommand.CONNECT);
        invalido.addNativeHeader("Authorization", "Bearer falso");
        assertThatThrownBy(() -> interceptor.preSend(message(invalido), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void usuarioAssinaApenasAPropriaFila() {
        interceptor.preSend(subscribe(customer, "/user/queue/events"), channel);

        assertThatThrownBy(() -> interceptor.preSend(subscribe(customer, "/topic/orders"), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void soEntregadorAssinaOTopicoDeCorridas() {
        interceptor.preSend(subscribe(driver, StompAuthInterceptor.DRIVERS_TOPIC), channel);

        assertThatThrownBy(() -> interceptor.preSend(subscribe(customer, StompAuthInterceptor.DRIVERS_TOPIC), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    @Test
    void clienteNaoEnviaMensagens() {
        StompHeaderAccessor send = StompHeaderAccessor.create(StompCommand.SEND);
        send.setUser(new StompPrincipal(customer));
        send.setDestination("/app/qualquer");

        assertThatThrownBy(() -> interceptor.preSend(message(send), channel))
                .isInstanceOf(MessageDeliveryException.class);
    }

    private static Message<?> subscribe(AuthenticatedUser user, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setUser(new StompPrincipal(user));
        accessor.setDestination(destination);
        return message(accessor);
    }

    private static Message<?> message(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
