package com.rota.notification.infrastructure.redis;

import com.rota.notification.application.port.SentNotifications;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

/**
 * {@code SET NX} com expiração: atômico entre instâncias do serviço e sem tabela para limpar.
 * Reentregas acontecem em segundos ou minutos; um dia de memória é folga suficiente.
 */
@Component
public class RedisSentNotifications implements SentNotifications {

    private static final Duration TTL = Duration.ofDays(1);

    private final StringRedisTemplate redis;

    public RedisSentNotifications(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean reserve(UUID eventId) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key(eventId), "1", TTL));
    }

    @Override
    public void release(UUID eventId) {
        redis.delete(key(eventId));
    }

    private static String key(UUID eventId) {
        return "notification:sent:" + eventId;
    }
}
