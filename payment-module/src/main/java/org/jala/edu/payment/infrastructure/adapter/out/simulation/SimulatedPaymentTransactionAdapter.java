
package org.jala.edu.payment.infrastructure.adapter.out.simulation;

import org.jala.edu.payment.application.port.out.PaymentTransactionPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.domain.model.PaymentTransaction;

import java.math.BigDecimal;
import java.util.UUID;

public class SimulatedPaymentTransactionAdapter
        implements PaymentTransactionPort {

    private final ManagePaymentHistoryUseCase historyUseCase;
    private final PaymentTransactionRepositoryPort transactionRepository;

    public SimulatedPaymentTransactionAdapter(
            ManagePaymentHistoryUseCase historyUseCase,
            PaymentTransactionRepositoryPort transactionRepository) {
        this.historyUseCase = historyUseCase;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public String recordApproved(
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount) {

        PaymentTransaction transaction =
                historyUseCase.recordPaymentAttempt(
                        orderId, userId, paymentMethodId, amount);

        if (!transaction.isApproved()) {
            throw new IllegalStateException(transaction.getReason());
        }

        return transaction.getId().value().toString();
    }

    @Override
    public void recordRejected(
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount,
            String reason) {

        if (orderId == null || userId == null
                || paymentMethodId == null || amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        PaymentTransaction rejected =
                PaymentTransaction.recordRejected(
                        orderId, userId, paymentMethodId, amount, reason);

        transactionRepository.save(rejected);
    }
}
