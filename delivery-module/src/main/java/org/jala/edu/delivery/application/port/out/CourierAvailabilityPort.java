package org.jala.edu.delivery.application.port.out;

import java.util.UUID;

/**
 * Knows whether a courier is available to take orders. A courier is available unless they were marked otherwise.
 */
public interface CourierAvailabilityPort {

    boolean isAvailable(UUID courierId);

    void markUnavailable(UUID courierId);
}
