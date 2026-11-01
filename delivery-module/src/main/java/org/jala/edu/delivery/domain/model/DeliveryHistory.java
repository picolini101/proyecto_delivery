package org.jala.edu.delivery.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * The finished deliveries of one courier, with the total amount they earned.
 */
public record DeliveryHistory(List<CompletedDelivery> deliveries) {

    public DeliveryHistory {
        deliveries = List.copyOf(deliveries);
    }

    public BigDecimal total() {
        return deliveries.stream()
                .map(CompletedDelivery::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return deliveries.isEmpty();
    }
}
