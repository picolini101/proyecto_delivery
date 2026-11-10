package org.jala.edu.delivery.application.port.out;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.delivery.domain.model.ActiveDelivery;

/**
 * Where a courier's shipment in progress is stored.
 */
public interface ActiveDeliveryPort {

    Optional<ActiveDelivery> findInProgressByCourier(UUID courierId);

    void add(ActiveDelivery delivery);
}
