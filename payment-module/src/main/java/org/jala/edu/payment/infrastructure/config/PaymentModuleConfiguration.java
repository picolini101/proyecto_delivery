package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionRepositoryPort;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.application.usecase.ManagePaymentMethodUseCase;
import org.jala.edu.payment.application.usecase.ProcessPaymentUseCase;
import org.jala.edu.payment.application.usecase.RetryPaymentUseCase;
import org.jala.edu.payment.infrastructure.adapter.in.service.PaymentServiceAdapter;
import org.jala.edu.payment.infrastructure.adapter.out.simulation.SimulatedPaymentMethodAdapter;
import org.jala.edu.payment.infrastructure.adapter.out.simulation.SimulatedPaymentTransactionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso del modulo de pagos como beans de Spring.
 */
@Configuration
public class PaymentModuleConfiguration {

    // HU #29: Gestion de metodos de pago
    @Bean
    public ManagePaymentMethodUseCase managePaymentMethodUseCase(
            PaymentMethodRepositoryPort repository,
            UserService userService) {
        return new ManagePaymentMethodUseCase(repository, userService);
    }

    // HU #33: Historial de transacciones
    @Bean
    public ManagePaymentHistoryUseCase managePaymentHistoryUseCase(
            PaymentTransactionRepositoryPort transactionRepository,
            UserService userService) {
        return new ManagePaymentHistoryUseCase(
                transactionRepository, userService);
    }

    // HU #30: Procesamiento de pagos simulados
    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            UserService userService,
            PaymentMethodValidationPort methodPort,
            PaymentTransactionPort transactionPort) {
        return new ProcessPaymentUseCase(
                userService, methodPort, transactionPort);
    }

    // HU #51: Reintento de pago rechazado
    @Bean
    public RetryPaymentUseCase retryPaymentUseCase(
            PaymentTransactionRepositoryPort transactionRepository,
            PaymentMethodRepositoryPort paymentMethodRepository,
            UserService userService) {
        return new RetryPaymentUseCase(
                transactionRepository, paymentMethodRepository, userService);
    }

    // Contrato PaymentService
    @Bean
    public PaymentService paymentService(
            ProcessPaymentUseCase useCase) {
        return new PaymentServiceAdapter(useCase);
    }

    // Validacion de metodos de pago
    @Bean
    public PaymentMethodValidationPort paymentMethodValidationPort(
            PaymentMethodRepositoryPort repository) {
        return new SimulatedPaymentMethodAdapter(repository);
    }

    // Registro de transacciones
    @Bean
    public PaymentTransactionPort paymentTransactionPort(
            ManagePaymentHistoryUseCase historyUseCase,
            PaymentTransactionRepositoryPort transactionRepository) {
        return new SimulatedPaymentTransactionAdapter(
                historyUseCase, transactionRepository);
    }

    // Carga de metodos de pago de ejemplo
    @Bean
    public SamplePaymentMethodsLoader samplePaymentMethodsLoader(
            PaymentMethodRepositoryPort repository) {
        return new SamplePaymentMethodsLoader(repository);
    }
}