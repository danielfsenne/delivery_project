package com.rota.restaurant.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.messaging.ProcessedEvents;
import com.rota.restaurant.domain.RestaurantRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RatingServiceTest {

    private final RestaurantRepository restaurants = mock(RestaurantRepository.class);
    private final ProcessedEvents processedEvents = mock(ProcessedEvents.class);
    private final RatingService service = new RatingService(restaurants, processedEvents);
    private final UUID eventId = UUID.randomUUID();

    @Test
    void primeiraEntregaSomaANota() {
        when(processedEvents.firstDelivery(eventId, RatingService.CONSUMER)).thenReturn(true);
        when(restaurants.addRating(1L, 5)).thenReturn(1);

        assertThat(service.addRating(eventId, 1L, 5)).isTrue();
        verify(restaurants).addRating(1L, 5);
    }

    @Test
    void eventoRepetidoNaoContaDuasVezes() {
        when(processedEvents.firstDelivery(eventId, RatingService.CONSUMER)).thenReturn(false);

        assertThat(service.addRating(eventId, 1L, 5)).isFalse();
        verify(restaurants, never()).addRating(any(), anyInt());
    }

    @Test
    void restauranteInexistenteFalhaParaIrADlq() {
        when(processedEvents.firstDelivery(any(), any())).thenReturn(true);
        when(restaurants.addRating(99L, 4)).thenReturn(0);

        assertThatThrownBy(() -> service.addRating(eventId, 99L, 4)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void notaForaDaEscalaEhRecusada() {
        assertThatThrownBy(() -> service.addRating(eventId, 1L, 6)).isInstanceOf(BusinessException.class);
    }
}
