package com.rota.delivery.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.delivery.application.DeliveryService;
import com.rota.delivery.application.DriverService;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.DeliveryResponse;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.DriverResponse;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.HistoryResponse;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.LocationRequest;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.StatusRequest;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.TrackingResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DriverService driverService;

    public DeliveryController(DeliveryService deliveryService, DriverService driverService) {
        this.deliveryService = deliveryService;
        this.driverService = driverService;
    }

    // --- Entregador --------------------------------------------------------

    @GetMapping("/deliveries/driver/me")
    public DriverResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return driverService.me(user.id());
    }

    @PutMapping("/deliveries/driver/status")
    public DriverResponse setStatus(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid @RequestBody StatusRequest request) {
        return driverService.setStatus(user.id(), request.status());
    }

    @PutMapping("/deliveries/driver/location")
    public DriverResponse updateLocation(@AuthenticationPrincipal AuthenticatedUser user,
                                         @Valid @RequestBody LocationRequest request) {
        return driverService.updateLocation(user.id(), request.latitude(), request.longitude());
    }

    @GetMapping("/deliveries/driver/history")
    public HistoryResponse history(@AuthenticationPrincipal AuthenticatedUser user) {
        return deliveryService.history(user.id());
    }

    @GetMapping("/deliveries/available")
    public List<DeliveryResponse> available(@AuthenticationPrincipal AuthenticatedUser user) {
        return deliveryService.available(user.id());
    }

    @PostMapping("/deliveries/{id}/accept")
    public DeliveryResponse accept(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return deliveryService.accept(user.id(), id);
    }

    @PostMapping("/deliveries/{id}/pickup")
    public DeliveryResponse pickUp(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return deliveryService.pickUp(user.id(), id);
    }

    @PostMapping("/deliveries/{id}/complete")
    public DeliveryResponse complete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return deliveryService.complete(user.id(), id);
    }

    // --- Cliente -----------------------------------------------------------

    @GetMapping("/deliveries/order/{orderId}")
    public TrackingResponse track(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId) {
        return deliveryService.track(user, orderId);
    }
}
