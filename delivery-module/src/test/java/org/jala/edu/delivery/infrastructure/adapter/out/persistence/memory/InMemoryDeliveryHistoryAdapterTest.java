package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.delivery.DeliveryFixtures.delivery;
import static org.jala.edu.delivery.DeliveryFixtures.deliveryOf;

import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.junit.jupiter.api.Test;

class InMemoryDeliveryHistoryAdapterTest {

    private final InMemoryDeliveryHistoryAdapter adapter = new InMemoryDeliveryHistoryAdapter();

    @Test
    void returnsOnlyTheDeliveriesOfTheGivenCourier() {
        // Arrange
        CompletedDelivery mine = delivery("10.00", "2026-10-20");
        adapter.add(mine);
        adapter.add(deliveryOf(UUID.randomUUID(), "5.00", "2026-10-21"));

        // Act
        var found = adapter.findByCourierId(SampleUsers.COURIER);

        // Assert
        assertThat(found).containsExactly(mine);
    }

    @Test
    void returnsAnEmptyListWhenTheCourierHasNoDeliveries() {
        // Arrange
        adapter.add(deliveryOf(UUID.randomUUID(), "5.00", "2026-10-21"));

        // Act
        var found = adapter.findByCourierId(SampleUsers.COURIER);

        // Assert
        assertThat(found).isEmpty();
    }
}
