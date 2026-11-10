package org.jala.edu.payment.infrastructure.adapter.in.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.application.usecase.ProcessPaymentUseCase;

public class PaymentServiceAdapter implements PaymentService {

    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentServiceAdapter(
            ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @Override
    public PaymentResult pay(
            UUID orderId,
            UUID userId,
            UUID paymentMethodId,
            BigDecimal amount) {

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                orderId,
                userId,
                paymentMethodId,
                amount
        );

        return processPaymentUseCase.execute(command);
    }
}
