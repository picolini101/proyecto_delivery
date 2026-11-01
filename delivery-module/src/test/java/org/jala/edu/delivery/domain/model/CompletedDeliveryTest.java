package org.jala.edu.delivery.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CompletedDeliveryTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);

    @Test
    void keepsTheDataOfTheFinishedDeliveryAndTrimsTheLocality() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CompletedDelivery delivery = new CompletedDelivery(orderId, SampleUsers.COURIER, new BigDecimal("10.00"),
                "  Centro ", DAY);

        // Assert
        assertThat(delivery.orderId()).isEqualTo(orderId);
        assertThat(delivery.courierId()).isEqualTo(SampleUsers.COURIER);
        assertThat(delivery.amount()).isEqualByComparingTo("10.00");
        assertThat(delivery.locality()).isEqualTo("Centro");
        assertThat(delivery.deliveredOn()).isEqualTo(DAY);
    }

    @Test
    void acceptsAZeroAmount() {
        // Arrange
        BigDecimal zero = BigDecimal.ZERO;

        // Act
        CompletedDelivery delivery = new CompletedDelivery(UUID.randomUUID(), SampleUsers.COURIER, zero, "Centro", DAY);

        // Assert
        assertThat(delivery.amount()).isZero();
    }

    @Test
    void rejectsANegativeAmount() {
        // Arrange
        BigDecimal negative = new BigDecimal("-0.01");

        // Act
        Throwable thrown = catchThrowable(
                () -> new CompletedDelivery(UUID.randomUUID(), SampleUsers.COURIER, negative, "Centro", DAY));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessageContaining("negative");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsABlankLocality(String locality) {
        // Arrange
        BigDecimal amount = BigDecimal.TEN;

        // Act
        Throwable thrown = catchThrowable(
                () -> new CompletedDelivery(UUID.randomUUID(), SampleUsers.COURIER, amount, locality, DAY));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessageContaining("Locality");
    }

    @Test
    void rejectsAMissingCourier() {
        // Arrange
        BigDecimal amount = BigDecimal.TEN;

        // Act
        Throwable thrown = catchThrowable(
                () -> new CompletedDelivery(UUID.randomUUID(), null, amount, "Centro", DAY));

        // Assert
        assertThat(thrown).isInstanceOf(NullPointerException.class).hasMessageContaining("Courier");
    }
}
