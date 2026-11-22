package org.jala.edu.order.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FailedPaymentRecordTest {

    @Test
    void shouldRestoreRecordKeepingItsIdAndDates() {
        // Arrange
        FailedPaymentRecordId id = FailedPaymentRecordId.newId();
        OrderId orderId = OrderId.newId();
        UUID customerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-11-21T10:15:30.123456Z");
        Instant updatedAt = Instant.parse("2026-11-21T10:16:00.000000Z");

        // Act
        FailedPaymentRecord record = FailedPaymentRecord.restore(
                id, orderId, customerId, PaymentAttemptResult.REJECTED, "Pago rechazado", createdAt, updatedAt);

        // Assert
        assertThat(record.getId()).isEqualTo(id);
        assertThat(record.getOrderId()).isEqualTo(orderId);
        assertThat(record.getCustomerId()).isEqualTo(customerId);
        assertThat(record.getResult()).isEqualTo(PaymentAttemptResult.REJECTED);
        assertThat(record.getReason()).isEqualTo("Pago rechazado");
        assertThat(record.getCreatedAt()).isEqualTo(createdAt);
        assertThat(record.getUpdatedAt()).isEqualTo(updatedAt);
    }
}