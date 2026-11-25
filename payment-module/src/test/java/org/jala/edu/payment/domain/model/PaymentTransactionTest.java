
package org.jala.edu.payment.domain.model;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias basicas para el modelo PaymentTransaction.
 */
class PaymentTransactionTest {

    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();

    @Test
    void recordsApprovedTransaction() {
        PaymentTransaction transaction = PaymentTransaction.recordApproved(
                orderId, userId, paymentMethodId, new BigDecimal("150.00"));

        assertNotNull(transaction.getId());
        assertEquals(orderId, transaction.getOrderId());
        assertEquals(userId, transaction.getUserId());
        assertEquals(paymentMethodId, transaction.getPaymentMethodId());
        assertEquals(new BigDecimal("150.00"), transaction.getAmount());
        assertEquals(PaymentStatus.APPROVED, transaction.getStatus());
        assertTrue(transaction.isApproved());
    }

    @Test
    void recordsRejectedTransactionWithReason() {
        PaymentTransaction transaction = PaymentTransaction.recordRejected(
                orderId, userId, paymentMethodId, new BigDecimal("150.00"),
                "Intento de cobro duplicado sobre pedido aprobado");

        assertEquals(PaymentStatus.REJECTED, transaction.getStatus());
        assertFalse(transaction.isApproved());
        assertEquals(
                "Intento de cobro duplicado sobre pedido aprobado",
                transaction.getReason());
    }

    @Test
    void rejectsZeroOrNegativeAmount() {
        assertThrows(DomainException.class, () ->
                PaymentTransaction.recordApproved(
                        orderId, userId, paymentMethodId, BigDecimal.ZERO));

        assertThrows(DomainException.class, () ->
                PaymentTransaction.recordApproved(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("-10.00")));
    }
}
