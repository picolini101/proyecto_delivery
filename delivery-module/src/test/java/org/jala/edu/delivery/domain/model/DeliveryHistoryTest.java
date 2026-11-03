package org.jala.edu.delivery.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.delivery.DeliveryFixtures.delivery;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeliveryHistoryTest {

    @Test
    void totalAddsUpTheAmountOfEveryDelivery() {
        // Arrange
        DeliveryHistory history = new DeliveryHistory(List.of(
                delivery("10.00", "2026-10-20"),
                delivery("12.50", "2026-10-22"),
                delivery("0.25", "2026-10-25")));

        // Act
        var total = history.total();

        // Assert
        assertThat(total).isEqualByComparingTo("22.75");
        assertThat(history.isEmpty()).isFalse();
    }

    @Test
    void anEmptyHistoryHasAZeroTotal() {
        // Arrange
        DeliveryHistory history = new DeliveryHistory(List.of());

        // Act
        var total = history.total();

        // Assert
        assertThat(history.isEmpty()).isTrue();
        assertThat(total).isZero();
    }

    @Test
    void keepsItsOwnCopyOfTheDeliveries() {
        // Arrange
        List<CompletedDelivery> deliveries = new ArrayList<>(List.of(delivery("10.00", "2026-10-20")));
        DeliveryHistory history = new DeliveryHistory(deliveries);

        // Act
        deliveries.add(delivery("99.00", "2026-10-21"));

        // Assert
        assertThat(history.deliveries()).hasSize(1);
        assertThat(history.total()).isEqualByComparingTo("10.00");
    }

    @Test
    void theListOfDeliveriesCannotBeChanged() {
        // Arrange
        DeliveryHistory history = new DeliveryHistory(List.of(delivery("10.00", "2026-10-20")));

        // Act
        Throwable thrown = catchThrowable(() -> history.deliveries().add(delivery("1.00", "2026-10-21")));

        // Assert
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }
}
