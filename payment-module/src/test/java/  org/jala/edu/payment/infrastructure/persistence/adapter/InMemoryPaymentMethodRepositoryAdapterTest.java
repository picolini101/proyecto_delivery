package org.jala.edu.payment.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.junit.jupiter.api.Test;

class InMemoryPaymentMethodRepositoryAdapterTest {

    private final InMemoryPaymentMethodRepositoryAdapter repository = new InMemoryPaymentMethodRepositoryAdapter();

    @Test
    void keepsOnlyTheNewestMethodAsLastRegisteredForThatUser() {
        UUID userId = UUID.randomUUID();
        PaymentMethod other = repository.save(PaymentMethod.createNew(UUID.randomUUID(), new Alias("Otro"), PaymentType.TARJETA, "1234"));
        PaymentMethod first = repository.save(PaymentMethod.createNew(userId, new Alias("Primera"), PaymentType.TARJETA, "1234"));

        repository.clearLastRegisteredFlag(userId);
        PaymentMethod second = repository.save(PaymentMethod.createNew(userId, new Alias("Segunda"), PaymentType.TRANSFERENCIA, "5678"));

        assertThat(repository.findByUserId(userId)).containsExactly(first, second);
        assertThat(repository.findLastRegisteredByUserId(userId)).contains(second);
        assertThat(other.isLastRegistered()).isTrue();
    }
}