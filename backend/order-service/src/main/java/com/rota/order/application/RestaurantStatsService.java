package com.rota.order.application;

import com.rota.common.security.AuthenticatedUser;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderRepository.SalesTotals;
import com.rota.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;

import static com.rota.order.domain.OrderStatus.DELIVERED;
import static com.rota.order.domain.OrderStatus.OUT_FOR_DELIVERY;
import static com.rota.order.domain.OrderStatus.PAID;
import static com.rota.order.domain.OrderStatus.PREPARING;
import static com.rota.order.domain.OrderStatus.READY_FOR_PICKUP;
import static com.rota.order.domain.OrderStatus.RESTAURANT_ACCEPTED;

/**
 * Indicadores do dia para o painel do restaurante. "Hoje" segue o fuso configurado.
 */
@Service
@Transactional(readOnly = true)
public class RestaurantStatsService {

    /** Pedidos pagos contam como venda; cancelados e aguardando pagamento não. */
    private static final Set<OrderStatus> SOLD = EnumSet.of(PAID, RESTAURANT_ACCEPTED, PREPARING, READY_FOR_PICKUP,
            OUT_FOR_DELIVERY, DELIVERED);

    private static final Set<OrderStatus> IN_PROGRESS = EnumSet.of(PAID, RESTAURANT_ACCEPTED, PREPARING,
            READY_FOR_PICKUP, OUT_FOR_DELIVERY);

    private final OrderRepository orders;
    private final Clock clock;
    private final ZoneId zone;

    public RestaurantStatsService(OrderRepository orders, Clock clock,
                                  @Value("${rota.timezone:America/Sao_Paulo}") ZoneId zone) {
        this.orders = orders;
        this.clock = clock;
        this.zone = zone;
    }

    public DailyStats today(AuthenticatedUser owner, Long restaurantId) {
        Instant startOfDay = LocalDate.now(clock.withZone(zone)).atStartOfDay(zone).toInstant();

        SalesTotals sales = orders.salesSince(restaurantId, owner.id(), startOfDay, SOLD);
        long cancelled = orders.countByRestaurantIdAndRestaurantOwnerIdAndCreatedAtGreaterThanEqualAndStatus(
                restaurantId, owner.id(), startOfDay, OrderStatus.CANCELLED);
        long inProgress = orders.countByRestaurantIdAndRestaurantOwnerIdAndStatusIn(restaurantId, owner.id(),
                IN_PROGRESS);

        BigDecimal revenue = sales.getRevenue().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal averageTicket = sales.getOrders() == 0
                ? BigDecimal.ZERO.setScale(2)
                : revenue.divide(BigDecimal.valueOf(sales.getOrders()), 2, RoundingMode.HALF_EVEN);

        return new DailyStats(sales.getOrders(), revenue, averageTicket, cancelled, inProgress);
    }

    public record DailyStats(long ordersToday, BigDecimal revenueToday, BigDecimal averageTicket,
                             long cancelledToday, long inProgress) {
    }
}
