package com.rota.restaurant.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OpeningHourTest {

    // 2026-01-05 é uma segunda-feira
    private static LocalDateTime monday(int hour, int minute) {
        return LocalDateTime.of(2026, 1, 5, hour, minute);
    }

    @Test
    void shouldCoverTimeInsideSameDayWindow() {
        OpeningHour lunch = new OpeningHour(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(15, 0));

        assertThat(lunch.covers(monday(11, 0))).isTrue();
        assertThat(lunch.covers(monday(15, 0))).isTrue();
        assertThat(lunch.covers(monday(10, 59))).isFalse();
        assertThat(lunch.covers(monday(15, 1))).isFalse();
        assertThat(lunch.covers(monday(12, 0).plusDays(1))).isFalse();
    }

    @Test
    void shouldHandleWindowThatCrossesMidnight() {
        OpeningHour night = new OpeningHour(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(2, 0));

        assertThat(night.covers(monday(23, 30))).isTrue();
        assertThat(night.covers(monday(1, 0).plusDays(1))).isTrue();
        assertThat(night.covers(monday(2, 0).plusDays(1))).isFalse();
        assertThat(night.covers(monday(1, 0))).isFalse();
    }

    @Test
    void shouldHandleSundayToMondayWrap() {
        OpeningHour sunday = new OpeningHour(DayOfWeek.SUNDAY, LocalTime.of(20, 0), LocalTime.of(1, 0));

        assertThat(sunday.covers(monday(0, 30))).isTrue();
    }
}
