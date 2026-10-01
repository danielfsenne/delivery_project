package com.rota.order.domain;

import java.time.Instant;

/**
 * Evento de domínio registrado pelo {@link Order} a cada mudança de status e publicado pelo
 * Spring Data quando o pedido é salvo. Guarda a referência ao pedido porque, na criação,
 * o id só existe depois do insert.
 *
 * @param previous status anterior; {@code null} na criação
 */
public record OrderStatusChangedEvent(Order order, OrderStatus previous, OrderStatus current, String reason,
                                      Instant occurredAt) {
}
