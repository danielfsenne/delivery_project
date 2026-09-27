package com.rota.order.interfaces.rest.dto;

import com.rota.order.domain.DeliveryAddress;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderHistory;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CheckoutRequest(
            @NotNull @Valid AddressRequest deliveryAddress,
            @NotNull PaymentMethod paymentMethod,
            @Size(max = 300) String notes
    ) {
    }

    public record AddressRequest(
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 20) String number,
            @Size(max = 80) String complement,
            @NotBlank @Size(max = 80) String district,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas") String state,
            @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String zipCode,
            Double latitude,
            Double longitude
    ) {
        public DeliveryAddress toAddress() {
            return new DeliveryAddress(street, number, complement, district, city, state, zipCode, latitude,
                    longitude);
        }
    }

    public record StatusChangeRequest(@NotNull OrderStatus status, @Size(max = 300) String reason) {
    }

    public record CancelRequest(@Size(max = 300) String reason) {
    }

    public record OrderResponse(
            Long id,
            OrderStatus status,
            Set<OrderStatus> nextStatuses,
            Long customerId,
            Long restaurantId,
            String restaurantName,
            Long driverId,
            PaymentMethod paymentMethod,
            List<ItemResponse> items,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal discount,
            BigDecimal total,
            String notes,
            DeliveryAddress deliveryAddress,
            List<HistoryResponse> history,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getStatus(), o.getStatus().nextStatuses(), o.getCustomerId(),
                    o.getRestaurantId(), o.getRestaurantName(), o.getDriverId(), o.getPaymentMethod(),
                    o.getItems().stream().map(ItemResponse::from).toList(), o.getSubtotal(), o.getDeliveryFee(),
                    o.getDiscount(), o.getTotal(), o.getNotes(), o.getDeliveryAddress(),
                    o.getHistory().stream().map(HistoryResponse::from).toList(), o.getCreatedAt(), o.getUpdatedAt());
        }
    }

    /** Versão enxuta para listagens. */
    public record OrderSummaryResponse(Long id, OrderStatus status, Long restaurantId, String restaurantName,
                                       int itemCount, BigDecimal total, Instant createdAt) {
        public static OrderSummaryResponse from(Order o) {
            return new OrderSummaryResponse(o.getId(), o.getStatus(), o.getRestaurantId(), o.getRestaurantName(),
                    o.getItems().stream().mapToInt(OrderItem::getQuantity).sum(), o.getTotal(), o.getCreatedAt());
        }
    }

    public record ItemResponse(Long productId, String name, String options, String notes, BigDecimal unitPrice,
                               int quantity, BigDecimal totalPrice) {
        static ItemResponse from(OrderItem i) {
            return new ItemResponse(i.getProductId(), i.getName(), i.getOptions(), i.getNotes(), i.getUnitPrice(),
                    i.getQuantity(), i.totalPrice());
        }
    }

    public record HistoryResponse(String event, OrderStatus oldStatus, OrderStatus newStatus, Long userId,
                                  String reason, Instant createdAt) {
        static HistoryResponse from(OrderHistory h) {
            return new HistoryResponse(h.getEvent(), h.getOldStatus(), h.getNewStatus(), h.getUserId(),
                    h.getReason(), h.getCreatedAt());
        }
    }
}
