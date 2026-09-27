package com.rota.restaurant.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Horário local usado para decidir se um restaurante está aberto.
 */
@Component
public class BusinessClock {

    private final Clock clock;
    private final ZoneId zone;

    public BusinessClock(Clock clock, @Value("${rota.timezone:America/Sao_Paulo}") ZoneId zone) {
        this.clock = clock;
        this.zone = zone;
    }

    public LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), zone);
    }
}
