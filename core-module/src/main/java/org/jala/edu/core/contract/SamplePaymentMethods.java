package org.jala.edu.core.contract;

import java.util.UUID;

/**
 * Fixed payment methods for the {@link SampleUsers}, so {@code order-module} can ask for a payment before
 * {@code payment-module} has a screen to register one (#86). {@code payment-module} stores them at startup once it
 * implements {@link PaymentService}; the fallback accepts them too.
 */
public final class SamplePaymentMethods {

    /** A card of {@link SampleUsers#CUSTOMER}. */
    public static final UUID CUSTOMER_CARD = UUID.fromString("00000000-0000-0000-0000-000000000401");

    private SamplePaymentMethods() {
    }
}
