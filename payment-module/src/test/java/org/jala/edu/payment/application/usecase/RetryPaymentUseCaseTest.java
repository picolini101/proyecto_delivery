package org.jala.edu.payment.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentStatus;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.domain.model.PaymentType;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentMethodRepositoryAdapter;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentTransactionRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para el caso de uso {@link RetryPaymentUseCase}.
 */
class RetryPaymentUseCaseTest {

    private InMemoryPaymentTransactionRepositoryAdapter transactionRepository;
    private InMemoryPaymentMethodRepositoryAdapter paymentMethodRepository;
    private RetryPaymentUseCase retryPaymentUseCase;
    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = SampleUsers.CUSTOMER;
    private PaymentMethod validMethod;

    @BeforeEach
    void setUp() {
        transactionRepository = new InMemoryPaymentTransactionRepositoryAdapter();
        paymentMethodRepository = new InMemoryPaymentMethodRepositoryAdapter();
        retryPaymentUseCase = new RetryPaymentUseCase(
                transactionRepository, paymentMethodRepository, new FallbackUserService());
        validMethod = PaymentMethod.createNew(
                userId, new Alias("Mi Visa"), PaymentType.TARJETA, "4111222233334444");
        paymentMethodRepository.save(validMethod);
    }

    @Test
    void approvesRetryWhenOrderHasRejectedAttemptAndMethodIsValid() {
        PaymentTransaction firstRejected = PaymentTransaction.recordRejected(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"), "Fondos insuficientes");
        transactionRepository.save(firstRejected);

        PaymentTransaction retryResult = retryPaymentUseCase.retryPayment(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"));

        assertTrue(retryResult.isApproved());
        assertEquals(PaymentStatus.APPROVED, retryResult.getStatus());
        assertNotEquals(firstRejected.getId(), retryResult.getId());
        List<PaymentTransaction> orderAttempts = transactionRepository.findByOrderId(orderId);
        assertEquals(2, orderAttempts.size());
    }

    @Test
    void rejectsRetryWhenPaymentMethodIsInactive() {
        PaymentTransaction firstRejected = PaymentTransaction.recordRejected(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"), "Error temporal");
        transactionRepository.save(firstRejected);
        validMethod.deactivate();
        paymentMethodRepository.save(validMethod);

        PaymentTransaction retryResult = retryPaymentUseCase.retryPayment(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"));

        assertFalse(retryResult.isApproved());
        assertEquals(PaymentStatus.REJECTED, retryResult.getStatus());
        assertEquals("Metodo de pago invalido o inactivo", retryResult.getReason());
        assertEquals(2, transactionRepository.findByOrderId(orderId).size());
    }

    @Test
    void rejectsRetryWhenOrderAlreadyHasApprovedPayment() {
        transactionRepository.save(PaymentTransaction.recordRejected(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"), "Fallo red"));
        transactionRepository.save(PaymentTransaction.recordApproved(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00")));

        PaymentTransaction retryResult = retryPaymentUseCase.retryPayment(
                orderId, userId, validMethod.getId(), new BigDecimal("85.00"));

        assertFalse(retryResult.isApproved());
        assertEquals(PaymentStatus.REJECTED, retryResult.getStatus());
        assertEquals("Intento de cobro duplicado sobre pedido aprobado", retryResult.getReason());
        assertEquals(3, transactionRepository.findByOrderId(orderId).size());
    }

    @Test
    void throwsExceptionWhenOrderHasNoPreviousRejectedAttempt() {
        assertThrows(DomainException.class, () ->
                retryPaymentUseCase.retryPayment(
                        orderId, userId, validMethod.getId(), new BigDecimal("85.00")));
    }

    @Test
    void throwsExceptionWhenUserIsNotAuthorized() {
        assertThrows(DomainException.class, () ->
                retryPaymentUseCase.retryPayment(
                        orderId, SampleUsers.INACTIVE_CUSTOMER,
                        validMethod.getId(), new BigDecimal("85.00")));
    }
}