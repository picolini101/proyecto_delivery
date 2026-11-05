package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Spring para el caso de uso de historial de pagos.
 */
@Configuration
public class PaymentModuleConfiguration {
    @Bean
    public ManagePaymentHistoryUseCase managePaymentHistoryUseCase(
            PaymentTransactionRepositoryPort transactionRepository,
            UserService userService) {
        return new ManagePaymentHistoryUseCase(transactionRepository, userService);
    }
}
