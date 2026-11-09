package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso CU-03: Consultar historial de transacciones y prevenir cobro doble.
 */
public class ManagePaymentHistoryUseCase {
    private final PaymentTransactionRepositoryPort transactionRepository;
    private final UserService userService;
    public ManagePaymentHistoryUseCase(PaymentTransactionRepositoryPort transactionRepository,
                                       UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }
    /**
     * Consulta el historial de transacciones de un usuario activo.
     */
    public List<PaymentTransaction> findByUserId(UUID userId) {
        validateActiveUser(userId);
        return transactionRepository.findByUserId(userId);
    }
    /**
     * Registra un intento de pago bloqueando cualquier cobro doble sobre un pedido ya aprobado.
     */
    public synchronized PaymentTransaction recordPaymentAttempt(UUID orderId, UUID userId,
                                                                UUID paymentMethodId, BigDecimal amount) {
        validateActiveUser(userId);
        if (transactionRepository.existsApprovedByOrderId(orderId)) {
            PaymentTransaction rejected = PaymentTransaction.recordRejected(
                    orderId, userId, paymentMethodId, amount, "Intento de cobro duplicado sobre pedido aprobado");
            return transactionRepository.save(rejected);
        }
        PaymentTransaction approved = PaymentTransaction.recordApproved(orderId, userId, paymentMethodId, amount);
        return transactionRepository.save(approved);
    }
    private void validateActiveUser(UUID userId) {
        if (!userService.isActiveWithRole(userId, UserRole.CUSTOMER)) {
            throw new DomainException("User not authorized for this operation");
        }
    }
}