package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.domain.model.CompletedDelivery;

/**
 * Keeps the finished deliveries in memory; data is lost when the application stops.
 */
public class InMemoryDeliveryHistoryAdapter implements DeliveryHistoryPort {

    private final List<CompletedDelivery> deliveries = new CopyOnWriteArrayList<>();

    public void add(CompletedDelivery delivery) {
        deliveries.add(delivery);
    }

    @Override
    public List<CompletedDelivery> findByCourierId(UUID courierId) {
        return deliveries.stream()
                .filter(delivery -> delivery.courierId().equals(courierId))
                .toList();
    }
}
