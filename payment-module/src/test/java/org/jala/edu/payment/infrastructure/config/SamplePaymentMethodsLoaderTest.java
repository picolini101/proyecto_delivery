package org.jala.edu.payment.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentMethodRepositoryAdapter;
import org.junit.jupiter.api.Test;

class SamplePaymentMethodsLoaderTest {

    private final InMemoryPaymentMethodRepositoryAdapter repository = new InMemoryPaymentMethodRepositoryAdapter();
    private final SamplePaymentMethodsLoader loader = new SamplePaymentMethodsLoader(repository);

    @Test
    void storesTheSampleCardOfTheSampleCustomer() {
        // Ejecución
        loader.load();

        // Verificación
        assertThat(repository.findById(SamplePaymentMethods.CUSTOMER_CARD)).hasValueSatisfying(card -> {
            assertThat(card.getUserId()).isEqualTo(SampleUsers.CUSTOMER);
            assertThat(card.getType()).isEqualTo(PaymentType.TARJETA);
            assertThat(card.isActive()).isTrue();
            assertThat(card.isLastRegistered()).isTrue();
        });
    }

    @Test
    void doesNotDuplicateTheCardWhenLoadedTwice() {
        // Ejecución
        loader.load();
        loader.load();

        // Verificación
        assertThat(repository.findByUserId(SampleUsers.CUSTOMER)).hasSize(1);
    }

    @Test
    void keepsTheLastRegisteredFlagOnTheCustomersExistingMethod() {
        // Verificación
        PaymentMethod existing = repository.save(PaymentMethod.createNew(
                SampleUsers.CUSTOMER, new Alias("Mi Nequi"), PaymentType.TRANSFERENCIA, "3001234567"));

        // Ejecución
        loader.load();

        // Verificación
        assertThat(repository.findById(SamplePaymentMethods.CUSTOMER_CARD))
                .hasValueSatisfying(card -> assertThat(card.isLastRegistered()).isFalse());
        assertThat(repository.findLastRegisteredByUserId(SampleUsers.CUSTOMER))
                .hasValueSatisfying(last -> assertThat(last.getId()).isEqualTo(existing.getId()));
    }
}
