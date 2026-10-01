package com.rota.common.events;

/**
 * Topologia dos eventos: um exchange do tipo topic e uma routing key por tipo de evento.
 * Cada consumidor declara a própria fila e escolhe as routing keys que quer receber.
 */
public final class RotaEvents {

    public static final String EXCHANGE = "rota.events";

    /** Mensagens que falharam em todas as tentativas vão para a DLQ de cada fila. */
    public static final String DEAD_LETTER_EXCHANGE = "rota.events.dlx";

    /** {@link OrderStatusChanged}: toda mudança de status de pedido. */
    public static final String ORDER_STATUS_CHANGED = "order.status-changed";

    /** {@link OrderReadyForPickup}: pedido pronto, abre a corrida para os entregadores. */
    public static final String ORDER_READY_FOR_PICKUP = "order.ready-for-pickup";

    /** {@link ReviewCreated}: cliente avaliou um pedido entregue. */
    public static final String REVIEW_CREATED = "review.created";

    /** {@link DeliveryStatusChanged}: corrida criada, aceita, retirada ou entregue. */
    public static final String DELIVERY_STATUS_CHANGED = "delivery.status-changed";

    /** {@link DriverLocationUpdated}: posição do entregador durante uma entrega. */
    public static final String DRIVER_LOCATION_UPDATED = "driver.location-updated";

    private RotaEvents() {
    }
}
