package org.jala.edu.payment.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.payment.application.dto.PaymentStatusResult;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentTransactionRepositoryAdapter;
import org.junit.jupiter.api.Test;

//Pruebas de la consulta del estado de pago de un pedido (HU #31).

class PaymentStatusQueryTest {

    private final InMemoryPaymentTransactionRepositoryAdapter repository =
            new InMemoryPaymentTransactionRepositoryAdapter();
    private final ManagePaymentHistoryUseCase useCase =
            new ManagePaymentHistoryUseCase(repository, new FallbackUserService());
    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = SampleUsers.CUSTOMER;
    private final UUID paymentMethodId = UUID.randomUUID();

    @Test
    void returnsApprovedStatusWithAmountAndDate() {
        PaymentTransaction approved = repository.save(PaymentTransaction.recordApproved(
                orderId, userId, paymentMethodId, new BigDecimal("120.00")));

        PaymentStatusResult result = useCase.findPaymentStatus(orderId, userId);

        assertEquals(PaymentStatusResult.APPROVED, result.status());
        assertEquals(new BigDecimal("120.00"), result.amount());
        assertEquals(approved.getCreatedAt(), result.attemptedAt());
    }

    @Test
    void returnsRejectedStatusWhenTheOnlyAttemptWasRejected() {
        repository.save(PaymentTransaction.recordRejected(
                orderId, userId, paymentMethodId, new BigDecimal("6000.00"), "El monto excede el limite permitido"));

        PaymentStatusResult result = useCase.findPaymentStatus(orderId, userId);

        assertEquals(PaymentStatusResult.REJECTED, result.status());
        assertEquals(new BigDecimal("6000.00"), result.amount());
    }

    @Test
    void returnsApprovedWhenOrderHasApprovedAttemptAndLaterRejection() {
        repository.save(PaymentTransaction.recordApproved(
                orderId, userId, paymentMethodId, new BigDecimal("120.00")));
        repository.save(PaymentTransaction.recordRejected(
                orderId, userId, paymentMethodId, new BigDecimal("120.00"),
                "Intento de cobro duplicado sobre pedido aprobado"));

        PaymentStatusResult result = useCase.findPaymentStatus(orderId, userId);

        assertEquals(PaymentStatusResult.APPROVED, result.status());
    }

    @Test
    void returnsPendingWithoutErrorWhenOrderHasNoAttempts() {
        PaymentStatusResult result = useCase.findPaymentStatus(orderId, userId);

        assertEquals(PaymentStatusResult.PENDING, result.status());
        assertNull(result.amount());
        assertNull(result.attemptedAt());
    }

    @Test
    void deniesQueryOnOrderPaidByAnotherUser() {
        UUID anotherUser = UUID.randomUUID();
        repository.save(PaymentTransaction.recordApproved(
                orderId, anotherUser, paymentMethodId, new BigDecimal("120.00")));

        DomainException exception = assertThrows(DomainException.class,
                () -> useCase.findPaymentStatus(orderId, userId));

        assertEquals("No tienes permiso para consultar el pago de este pedido", exception.getMessage());
    }

    @Test
    void rejectsInactiveUser() {
        DomainException exception = assertThrows(DomainException.class,
                () -> useCase.findPaymentStatus(orderId, SampleUsers.INACTIVE_CUSTOMER));

        assertEquals("User not authorized for this operation", exception.getMessage());
    }
}