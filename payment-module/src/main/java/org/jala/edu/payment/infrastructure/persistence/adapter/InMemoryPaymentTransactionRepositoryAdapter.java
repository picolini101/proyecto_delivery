package org.jala.edu.payment.infrastructure.persistence.adapter;

import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.domain.model.PaymentTransactionId;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador en memoria para guardar y consultar transacciones de pago.
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryPaymentTransactionRepositoryAdapter implements PaymentTransactionRepositoryPort {
    private final Map<PaymentTransactionId, PaymentTransaction> store = new LinkedHashMap<>();
    @Override
    public synchronized PaymentTransaction save(PaymentTransaction transaction) {
        store.put(transaction.getId(), transaction);
        return transaction;
    }
    @Override
    public synchronized List<PaymentTransaction> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(transaction -> transaction.getUserId().equals(userId))
                .toList();
    }
    @Override
    public synchronized List<PaymentTransaction> findByOrderId(UUID orderId) {
        return store.values().stream()
                .filter(transaction -> transaction.getOrderId().equals(orderId))
                .toList();
    }
    @Override
    public synchronized boolean existsApprovedByOrderId(UUID orderId) {
        return store.values().stream()
                .anyMatch(transaction -> transaction.getOrderId().equals(orderId) && transaction.isApproved());
    }
}
