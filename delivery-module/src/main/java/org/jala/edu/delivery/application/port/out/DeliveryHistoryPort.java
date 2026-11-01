package org.jala.edu.delivery.application.port.out;

import java.util.List;
import java.util.UUID;
import org.jala.edu.delivery.domain.model.CompletedDelivery;

/**
 * Where the finished deliveries are read from.
 */
public interface DeliveryHistoryPort {

    /**
     * Returns the finished deliveries of the given courier, or an empty list when there are none.
     */
    List<CompletedDelivery> findByCourierId(UUID courierId);
}
