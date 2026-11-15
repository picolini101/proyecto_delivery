package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentStatus;
import org.jala.edu.payment.domain.model.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para reintentar el pago de un pedido cuyo intento previo fue rechazado.
 */
public class RetryPaymentUseCase {
    private final PaymentTransactionRepositoryPort transactionRepository;
    private final PaymentMethodRepositoryPort paymentMethodRepository;
    private final UserService userService;
    public RetryPaymentUseCase(PaymentTransactionRepositoryPort transactionRepository,
                               PaymentMethodRepositoryPort paymentMethodRepository,
                               UserService userService) {
        this.transactionRepository = transactionRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.userService = userService;
    }
    /**
     * Procesa un nuevo intento de pago sobre un pedido con pago rechazado previo.
     */
    public synchronized PaymentTransaction retryPayment(UUID orderId, UUID userId,
                                                        UUID paymentMethodId, BigDecimal amount) {
        validateActiveUser(userId);
        List<PaymentTransaction> previousAttempts = transactionRepository.findByOrderId(orderId);
        boolean hasRejectedAttempt = previousAttempts.stream()
                .anyMatch(transaction -> transaction.getStatus() == PaymentStatus.REJECTED);
        if (!hasRejectedAttempt) {
            throw new DomainException("El pedido no tiene un pago rechazado para reintentar");
        }
        if (transactionRepository.existsApprovedByOrderId(orderId)) {
            PaymentTransaction rejectedDuplicate = PaymentTransaction.recordRejected(
                    orderId, userId, paymentMethodId, amount, "Intento de cobro duplicado sobre pedido aprobado");
            return transactionRepository.save(rejectedDuplicate);
        }
        if (!isValidPaymentMethod(userId, paymentMethodId)) {
            PaymentTransaction rejectedMethod = PaymentTransaction.recordRejected(
                    orderId, userId, paymentMethodId, amount, "Metodo de pago invalido o inactivo");
            return transactionRepository.save(rejectedMethod);
        }
        PaymentTransaction approvedRetry = PaymentTransaction.recordApproved(
                orderId, userId, paymentMethodId, amount);
        return transactionRepository.save(approvedRetry);
    }
    private boolean isValidPaymentMethod(UUID userId, UUID paymentMethodId) {
        if (paymentMethodId == null) {
            return false;
        }
        Optional<PaymentMethod> methodOpt = paymentMethodRepository.findById(paymentMethodId);
        if (methodOpt.isEmpty()) {
            return false;
        }
        PaymentMethod method = methodOpt.get();
        return method.getUserId().equals(userId) && method.isActive();
    }
    private void validateActiveUser(UUID userId) {
        if (!userService.isActiveWithRole(userId, UserRole.CUSTOMER)) {
            throw new DomainException("User not authorized for this operation");
        }
    }
}