package com.rota.notification.infrastructure.websocket;

import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * O navegador não envia headers no handshake do WebSocket, então o token vai no frame CONNECT.
 * Depois, cada SUBSCRIBE é conferido: um usuário só assina a própria fila ({@code /user/queue/...})
 * e só entregadores assinam o tópico de corridas.
 */
public class StompAuthInterceptor implements ChannelInterceptor {

    public static final String DRIVERS_TOPIC = "/topic/drivers/deliveries";
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    public StompAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        switch (accessor.getCommand()) {
            case CONNECT -> accessor.setUser(new StompPrincipal(authenticate(accessor)));
            case SUBSCRIBE -> checkSubscription(accessor);
            case SEND -> throw new MessageDeliveryException("Este canal é somente leitura");
            default -> {
                // DISCONNECT, UNSUBSCRIBE e heartbeats passam.
            }
        }
        return message;
    }

    private AuthenticatedUser authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            throw new MessageDeliveryException("Token ausente");
        }
        return jwtService.parse(header.substring(BEARER.length()))
                .orElseThrow(() -> new MessageDeliveryException("Token inválido ou expirado"));
    }

    private static void checkSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof StompPrincipal principal)) {
            throw new MessageDeliveryException("Sessão não autenticada");
        }
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessageDeliveryException("Destino ausente");
        }
        boolean ownQueue = destination.startsWith("/user/queue/");
        boolean driversTopic = destination.equals(DRIVERS_TOPIC) && principal.user().hasRole(Role.DRIVER);
        if (!ownQueue && !driversTopic) {
            throw new MessageDeliveryException("Sem permissão para assinar " + destination);
        }
    }
}
