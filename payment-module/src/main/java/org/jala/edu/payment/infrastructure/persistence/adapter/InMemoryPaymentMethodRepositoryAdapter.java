package org.jala.edu.payment.infrastructure.persistence.adapter;

import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.repository.PaymentMethodRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnInMemoryPersistence
public class InMemoryPaymentMethodRepositoryAdapter implements PaymentMethodRepository {

    private final Map<UUID, PaymentMethod> store = new LinkedHashMap<>();

    @Override
    public synchronized PaymentMethod save(PaymentMethod paymentMethod) {
        store.put(paymentMethod.getId(), paymentMethod);
        return paymentMethod;
    }

    @Override
    public synchronized Optional<PaymentMethod> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<PaymentMethod> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(method -> method.getUserId().equals(userId))
                .toList();
    }

    @Override
    public synchronized Optional<PaymentMethod> findLastRegisteredByUserId(UUID userId) {
        return findByUserId(userId).stream()
                .filter(PaymentMethod::isLastRegistered)
                .findFirst();
    }

    @Override
    public synchronized void clearLastRegisteredFlag(UUID userId) {
        findByUserId(userId).forEach(PaymentMethod::unmarkAsLastRegistered);
    }
}