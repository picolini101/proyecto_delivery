package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.application.usecase.ManagePaymentMethodUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso del módulo de pagos como beans de Spring.
 */
@Configuration
public class PaymentModuleConfiguration {

    @Bean
    public ManagePaymentMethodUseCase managePaymentMethodUseCase(
            PaymentMethodRepositoryPort repository, UserService userService) {
        return new ManagePaymentMethodUseCase(repository, userService);
    }

    @Bean
    public ManagePaymentHistoryUseCase managePaymentHistoryUseCase(
            PaymentTransactionRepositoryPort transactionRepository, UserService userService) {
        return new ManagePaymentHistoryUseCase(transactionRepository, userService);
    }

    @Bean
    public SamplePaymentMethodsLoader samplePaymentMethodsLoader(PaymentMethodRepositoryPort repository) {
        return new SamplePaymentMethodsLoader(repository);
    }
}