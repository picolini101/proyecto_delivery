package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

// Guarda al arrancar la tarjeta de prueba SamplePaymentMethods.CUSTOMER_CARD de SampleUsers.CUSTOMER (#86),
// con su id fijo, para que order-module pueda pagar con ella. Si ya existe, no la toca.

public class SamplePaymentMethodsLoader implements ApplicationRunner {

    static final String SAMPLE_CARD_ALIAS = "Tarjeta de prueba";
    static final String SAMPLE_CARD_MASKED_NUMBER = "**** **** **** 0401";

    private final PaymentMethodRepositoryPort repository;

    public SamplePaymentMethodsLoader(PaymentMethodRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        load();
    }

    void load() {
        // Con -Pjpa la base conserva datos entre arranques
        if (repository.findById(SamplePaymentMethods.CUSTOMER_CARD).isPresent()) {
            return;
        }
        // Solo queda como último registrado si el cliente no tenía otro marcado (D-04: uno solo marcado)
        boolean noLastRegistered = repository.findLastRegisteredByUserId(SampleUsers.CUSTOMER).isEmpty();
        repository.save(new PaymentMethod(
                SamplePaymentMethods.CUSTOMER_CARD,
                SampleUsers.CUSTOMER,
                new Alias(SAMPLE_CARD_ALIAS),
                PaymentType.TARJETA,
                SAMPLE_CARD_MASKED_NUMBER,
                noLastRegistered,
                true));
    }
}
