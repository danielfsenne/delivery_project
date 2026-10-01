package com.rota.delivery.application;

import com.rota.common.exception.BusinessException;
import com.rota.delivery.application.port.LocationBroadcaster;
import com.rota.delivery.domain.Delivery;
import com.rota.delivery.domain.DeliveryRepository;
import com.rota.delivery.domain.DeliveryStatus;
import com.rota.delivery.domain.Driver;
import com.rota.delivery.domain.DriverRepository;
import com.rota.delivery.domain.DriverStatus;
import com.rota.delivery.domain.GeoPoint;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.DriverResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.EnumSet;

/**
 * Perfil operacional do entregador. É criado no primeiro acesso: basta ter o papel DRIVER.
 */
@Service
@Transactional
public class DriverService {

    static final EnumSet<DeliveryStatus> ACTIVE = EnumSet.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP);

    private final DriverRepository drivers;
    private final DeliveryRepository deliveries;
    private final LocationBroadcaster locationBroadcaster;
    private final Clock clock;

    public DriverService(DriverRepository drivers, DeliveryRepository deliveries,
                         LocationBroadcaster locationBroadcaster, Clock clock) {
        this.drivers = drivers;
        this.deliveries = deliveries;
        this.locationBroadcaster = locationBroadcaster;
        this.clock = clock;
    }

    public DriverResponse me(Long driverId) {
        return view(getOrCreate(driverId));
    }

    public DriverResponse setStatus(Long driverId, DriverStatus status) {
        Driver driver = getOrCreate(driverId);
        switch (status) {
            case ONLINE -> driver.goOnline(clock.instant());
            case OFFLINE -> driver.goOffline(clock.instant());
            case BUSY -> throw new BusinessException("O status ocupado é definido automaticamente");
        }
        return view(driver);
    }

    /**
     * Durante uma entrega, a nova posição também é repassada ao cliente que acompanha o pedido.
     */
    public DriverResponse updateLocation(Long driverId, double latitude, double longitude) {
        Driver driver = getOrCreate(driverId);
        driver.moveTo(new GeoPoint(latitude, longitude), clock.instant());
        deliveries.findFirstByDriverIdAndStatusIn(driverId, ACTIVE).ifPresent(delivery ->
                locationBroadcaster.driverMoved(driverId, delivery.getOrderId(), delivery.getCustomerId(),
                        latitude, longitude));
        return view(driver);
    }

    Driver getOrCreate(Long driverId) {
        return drivers.findById(driverId).orElseGet(() -> drivers.save(new Driver(driverId, clock.instant())));
    }

    private DriverResponse view(Driver driver) {
        Delivery current = deliveries.findFirstByDriverIdAndStatusIn(driver.getId(), ACTIVE).orElse(null);
        return DriverResponse.from(driver, current,
                deliveries.countByDriverIdAndStatus(driver.getId(), DeliveryStatus.DELIVERED),
                deliveries.totalEarnings(driver.getId()));
    }
}
