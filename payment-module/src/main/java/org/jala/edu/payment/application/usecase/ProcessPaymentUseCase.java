package org.jala.edu.payment.application.usecase;

import java.math.BigDecimal;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.domain.repository.PaymentMethodRepository;

public class ProcessPaymentUseCase {

    private final UserService userService;
    private final PaymentMethodRepository paymentMethodRepository;

    public ProcessPaymentUseCase(
            UserService userService,
            PaymentMethodRepository paymentMethodRepository) {
        this.userService = userService;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public PaymentResult execute(ProcessPaymentCommand command) {
        if (!userService.isActiveWithRole(command.userId(), UserRole.CUSTOMER)) {
            return new PaymentResult(
                    "",
                    false,
                    "User not authorized for this operation"
            );
        }

        boolean hasValidMethod = paymentMethodRepository
                .findLastRegisteredByUserId(command.userId())
                .filter(paymentMethod -> paymentMethod.isActive())
                .isPresent();

        if (!hasValidMethod) {
            return new PaymentResult("", false, "sin método de pago");
        }

        BigDecimal amount = command.amount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult("", false, "El monto debe ser mayor a cero");
        }

        if (amount.compareTo(new BigDecimal("5000.00")) > 0) {
            return new PaymentResult(
                    "",
                    false,
                    "El monto excede el límite permitido"
            );
        }

        PaymentTransaction transaction = PaymentTransaction.recordApproved(
                command.orderId(),
                command.userId(),
                amount
        );

        return new PaymentResult(
                transaction.getId().toString(),
                true,
                ""
        );
    }
}