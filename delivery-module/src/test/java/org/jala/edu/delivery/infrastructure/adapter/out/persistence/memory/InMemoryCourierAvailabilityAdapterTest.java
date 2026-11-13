package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryCourierAvailabilityAdapterTest {

    private final InMemoryCourierAvailabilityAdapter availability = new InMemoryCourierAvailabilityAdapter();

    @Test
    void aCourierIsAvailableByDefault() {
        assertThat(availability.isAvailable(UUID.randomUUID())).isTrue();
    }

    @Test
    void aCourierMarkedUnavailableIsNoLongerAvailable() {
        // Arrange
        UUID courierId = UUID.randomUUID();
        UUID otherCourierId = UUID.randomUUID();

        // Act
        availability.markUnavailable(courierId);

        // Assert
        assertThat(availability.isAvailable(courierId)).isFalse();
        assertThat(availability.isAvailable(otherCourierId)).isTrue();
    }
}
