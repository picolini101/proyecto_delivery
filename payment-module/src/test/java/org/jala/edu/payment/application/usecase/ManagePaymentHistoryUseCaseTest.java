
package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.payment.domain.model.PaymentStatus;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentTransactionRepositoryAdapter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para el caso de uso ManagePaymentHistoryUseCase.
 */
class ManagePaymentHistoryUseCaseTest {

    private final InMemoryPaymentTransactionRepositoryAdapter repository =
            new InMemoryPaymentTransactionRepositoryAdapter();

    private final ManagePaymentHistoryUseCase useCase =
            new ManagePaymentHistoryUseCase(
                    repository, new FallbackUserService());

    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = SampleUsers.CUSTOMER;
    private final UUID paymentMethodId = UUID.randomUUID();

    @Test
    void approvesFirstPaymentAndReturnsUserHistory() {
        PaymentTransaction transaction = useCase.recordPaymentAttempt(
                orderId, userId, paymentMethodId, new BigDecimal("120.00"));

        assertTrue(transaction.isApproved());
        assertEquals(PaymentStatus.APPROVED, transaction.getStatus());

        List<PaymentTransaction> history = useCase.findByUserId(userId);

        assertEquals(1, history.size());
        assertEquals(orderId, history.get(0).getOrderId());
    }

    @Test
    void rejectsDuplicateChargeOnAlreadyApprovedOrder() {
        PaymentTransaction firstAttempt = useCase.recordPaymentAttempt(
                orderId, userId, paymentMethodId, new BigDecimal("120.00"));

        PaymentTransaction duplicateAttempt = useCase.recordPaymentAttempt(
                orderId, userId, paymentMethodId, new BigDecimal("120.00"));

        assertTrue(firstAttempt.isApproved());
        assertFalse(duplicateAttempt.isApproved());
        assertEquals(PaymentStatus.REJECTED, duplicateAttempt.getStatus());

        assertEquals(
                "Intento de cobro duplicado sobre pedido aprobado",
                duplicateAttempt.getReason());

        List<PaymentTransaction> history = useCase.findByUserId(userId);

        assertEquals(2, history.size());
    }

    @Test
    void rejectsInactiveOrUnauthorizedUser() {
        assertThrows(DomainException.class, () ->
                useCase.findByUserId(SampleUsers.INACTIVE_CUSTOMER));

        assertThrows(DomainException.class, () ->
                useCase.recordPaymentAttempt(
                        orderId,
                        SampleUsers.INACTIVE_CUSTOMER,
                        paymentMethodId,
                        new BigDecimal("120.00")));
    }
}
