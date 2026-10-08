package com.rota.order.application;

import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderRepository.SalesTotals;
import com.rota.order.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Indicadores do dia da plataforma inteira, para o painel do administrador.
 * Usa as mesmas regras de venda do painel do restaurante ({@link RestaurantStatsService}).
 */
@Service
@Transactional(readOnly = true)
public class PlatformStatsService {

    private static final int TOP_RESTAURANTS = 5;

    private final OrderRepository orders;
    private final Clock clock;
    private final ZoneId zone;

    public PlatformStatsService(OrderRepository orders, Clock clock,
                                @Value("${rota.timezone:America/Sao_Paulo}") ZoneId zone) {
        this.orders = orders;
        this.clock = clock;
        this.zone = zone;
    }

    public PlatformStats today() {
        Instant startOfDay = LocalDate.now(clock.withZone(zone)).atStartOfDay(zone).toInstant();

        SalesTotals sales = orders.platformSalesSince(startOfDay, RestaurantStatsService.SOLD);
        BigDecimal revenue = sales.getRevenue().setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal averageTicket = sales.getOrders() == 0
                ? BigDecimal.ZERO.setScale(2)
                : revenue.divide(BigDecimal.valueOf(sales.getOrders()), 2, RoundingMode.HALF_EVEN);

        List<TopRestaurant> top = orders.topRestaurantsSince(startOfDay, RestaurantStatsService.SOLD,
                        PageRequest.of(0, TOP_RESTAURANTS)).stream()
                .map(r -> new TopRestaurant(r.getRestaurantId(), r.getRestaurantName(), r.getOrders(),
                        r.getRevenue().setScale(2, RoundingMode.HALF_EVEN)))
                .toList();

        return new PlatformStats(sales.getOrders(), revenue, averageTicket,
                orders.countByCreatedAtGreaterThanEqualAndStatus(startOfDay, OrderStatus.CANCELLED),
                orders.countByStatusIn(RestaurantStatsService.IN_PROGRESS),
                orders.countByStatusIn(List.of(OrderStatus.PAYMENT_PENDING)),
                top);
    }

    /**
     * @param awaitingPayment pedidos parados em PAYMENT_PENDING (ex.: pagamento estava fora do ar)
     */
    public record PlatformStats(long ordersToday, BigDecimal revenueToday, BigDecimal averageTicket,
                                long cancelledToday, long inProgress, long awaitingPayment,
                                List<TopRestaurant> topRestaurants) {
    }

    public record TopRestaurant(Long restaurantId, String restaurantName, long orders, BigDecimal revenue) {
    }
}
