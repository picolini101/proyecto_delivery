package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.dto.PaymentStatusResult;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Caso de uso CU-03: Consultar historial de transacciones y prevenir cobro doble.

public class ManagePaymentHistoryUseCase {
    private final PaymentTransactionRepositoryPort transactionRepository;
    private final UserService userService;
    public ManagePaymentHistoryUseCase(PaymentTransactionRepositoryPort transactionRepository,
                                       UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }
    // Consulta el historial de transacciones de un usuario activo.

    public List<PaymentTransaction> findByUserId(UUID userId) {
        validateActiveUser(userId);
        return transactionRepository.findByUserId(userId);
    }

    // Consulta el estado del pago de un pedido del usuario (HU #31).
    // Sin intentos devuelve PENDIENTE; con un intento aprobado devuelve APROBADO.
    // si solo hay rechazos devuelve el ultimo intento como RECHAZADO.

    public PaymentStatusResult findPaymentStatus(UUID orderId, UUID userId) {
        validateActiveUser(userId);
        List<PaymentTransaction> attempts = transactionRepository.findByOrderId(orderId);
        if (attempts.isEmpty()) {
            return new PaymentStatusResult(orderId, PaymentStatusResult.PENDING, null, null);
        }
        validateOwner(attempts, userId);
        PaymentTransaction shown = selectAttemptToShow(attempts);
        String status = shown.isApproved() ? PaymentStatusResult.APPROVED : PaymentStatusResult.REJECTED;
        return new PaymentStatusResult(orderId, status, shown.getAmount(), shown.getCreatedAt());
    }

    private void validateOwner(List<PaymentTransaction> attempts, UUID userId) {
        for (PaymentTransaction attempt : attempts) {
            if (!attempt.getUserId().equals(userId)) {
                throw new DomainException("No tienes permiso para consultar el pago de este pedido");
            }
        }
    }

    private PaymentTransaction selectAttemptToShow(List<PaymentTransaction> attempts) {
        PaymentTransaction latest = attempts.get(0);
        for (PaymentTransaction attempt : attempts) {
            if (attempt.isApproved()) {
                return attempt;
            }
            if (attempt.getCreatedAt().isAfter(latest.getCreatedAt())) {
                latest = attempt;
            }
        }
        return latest;
    }

    //Registra un intento de pago bloqueando cualquier cobro doble sobre un pedido ya aprobado.

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