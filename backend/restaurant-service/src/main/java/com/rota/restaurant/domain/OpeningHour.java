package com.rota.restaurant.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Janela de funcionamento em um dia da semana. Se {@code closesAt} for anterior a
 * {@code opensAt}, o expediente atravessa a meia-noite (ex.: 18:00 às 02:00).
 */
@Embeddable
public record OpeningHour(
        @Enumerated(EnumType.STRING) @Column(name = "day_of_week") DayOfWeek dayOfWeek,
        @Column(name = "opens_at") LocalTime opensAt,
        @Column(name = "closes_at") LocalTime closesAt
) {

    public boolean crossesMidnight() {
        return closesAt.isBefore(opensAt);
    }

    public boolean covers(LocalDateTime moment) {
        DayOfWeek day = moment.getDayOfWeek();
        LocalTime time = moment.toLocalTime();
        if (!crossesMidnight()) {
            return day == dayOfWeek && !time.isBefore(opensAt) && !time.isAfter(closesAt);
        }
        boolean sameDayLateNight = day == dayOfWeek && !time.isBefore(opensAt);
        boolean nextDayEarlyMorning = day == dayOfWeek.plus(1) && time.isBefore(closesAt);
        return sameDayLateNight || nextDayEarlyMorning;
    }
}
