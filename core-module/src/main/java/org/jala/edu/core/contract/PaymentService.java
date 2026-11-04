package org.jala.edu.core.contract;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated payment offered by {@code payment-module}; {@code order-module} calls it when an order is confirmed.
 */
public interface PaymentService {

    /**
     * Pays {@code amount} for the order with one of the user's payment methods (FUN-002, #86).
     *
     * @param paymentMethodId a method the user registered in {@code payment-module}; until there is a screen to choose
     *                        it, use {@link SamplePaymentMethods#CUSTOMER_CARD} for {@link SampleUsers#CUSTOMER}
     */
    PaymentResult pay(UUID orderId, UUID userId, UUID paymentMethodId, BigDecimal amount);
}
