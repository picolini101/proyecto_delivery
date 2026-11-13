package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jala.edu.delivery.application.port.out.CourierAvailabilityPort;

/**
 * Keeps in memory the couriers that are not available; data is lost when the application stops.
 */
public class InMemoryCourierAvailabilityAdapter implements CourierAvailabilityPort {

    private final Set<UUID> unavailableCouriers = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isAvailable(UUID courierId) {
        return !unavailableCouriers.contains(courierId);
    }

    @Override
    public void markUnavailable(UUID courierId) {
        unavailableCouriers.add(courierId);
    }
}
