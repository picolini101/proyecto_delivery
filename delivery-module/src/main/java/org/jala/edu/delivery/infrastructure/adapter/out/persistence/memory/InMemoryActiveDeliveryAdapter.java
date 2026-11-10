package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.model.ActiveDelivery;

/**
 * Keeps shipments in progress in memory; data is lost when the application stops.
 */
public class InMemoryActiveDeliveryAdapter implements ActiveDeliveryPort {

    private final CopyOnWriteArrayList<ActiveDelivery> deliveries = new CopyOnWriteArrayList<>();

    @Override
    public Optional<ActiveDelivery> findInProgressByCourier(UUID courierId) {
        return deliveries.stream()
                .filter(delivery -> delivery.courierId().equals(courierId))
                .filter(ActiveDelivery::isInProgress)
                .findFirst();
    }

    @Override
    public void add(ActiveDelivery delivery) {
        deliveries.add(delivery);
    }

    @Override
    public void update(ActiveDelivery delivery) {
        for (int index = 0; index < deliveries.size(); index++) {
            if (deliveries.get(index).orderId().equals(delivery.orderId())) {
                deliveries.set(index, delivery);
                return;
            }
        }
        throw new NotFoundException("Delivery not found: " + delivery.orderId());
    }
}
