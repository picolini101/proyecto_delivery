package org.jala.edu.delivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.delivery.domain.model.CompletedDelivery;

/**
 * Test data for the Arrange step. Builds valid deliveries with sensible defaults,
 * so each test only spells out the values that matter to it.
 */
public final class DeliveryFixtures {

    public static final String LOCALITY = "Centro";

    private DeliveryFixtures() {
    }

    /**
     * A delivery of the sample courier with the given amount, delivered on the given day.
     */
    public static CompletedDelivery delivery(String amount, String deliveredOn) {
        return deliveryOf(SampleUsers.COURIER, amount, deliveredOn);
    }

    public static CompletedDelivery deliveryOf(UUID courierId, String amount, String deliveredOn) {
        return new CompletedDelivery(UUID.randomUUID(), courierId, new BigDecimal(amount), LOCALITY,
                LocalDate.parse(deliveredOn));
    }
}
