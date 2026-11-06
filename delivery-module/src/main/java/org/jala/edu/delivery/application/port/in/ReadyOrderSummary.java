package org.jala.edu.delivery.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.core.contract.ReadyOrder;

/**
 * One order on the courier dashboard. The shipping cost is copied from {@link ReadyOrder};
 * this module never calculates it.
 */
public record ReadyOrderSummary(UUID orderId, String deliveryAddress, BigDecimal shippingCost) {

    public static ReadyOrderSummary copyOf(ReadyOrder order) {
        return new ReadyOrderSummary(order.orderId(), order.deliveryAddress(), order.shippingCost());
    }
}
