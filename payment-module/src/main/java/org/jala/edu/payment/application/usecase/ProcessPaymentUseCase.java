
package org.jala.edu.payment.application.usecase;

import java.math.BigDecimal;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.domain.repository.PaymentMethodRepository;

public class ProcessPaymentUseCase {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("5000.00");

    private final UserService userService;
    private final PaymentMethodRepository paymentMethodRepository;

    public ProcessPaymentUseCase(
            UserService userService,
            PaymentMethodRepository paymentMethodRepository) {
        this.userService = userService;
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public PaymentResult execute(ProcessPaymentCommand command) {

        if (command == null) {
            return new PaymentResult("", false, "La solicitud de pago es obligatoria");
        }

        if (command.orderId() == null) {
            return new PaymentResult("", false, "El pedido es obligatorio");
        }

        if (command.userId() == null) {
            return new PaymentResult("", false, "El usuario es obligatorio");
        }

        BigDecimal amount = command.amount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult("", false, "El monto debe ser mayor a cero");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            return new PaymentResult("", false, "El monto excede el límite permitido");
        }

        if (!userService.isActiveWithRole(command.userId(), UserRole.CUSTOMER)) {
            return new PaymentResult(
                    "", false, "Usuario no autorizado para realizar pagos"
            );
        }

        boolean hasValidMethod = paymentMethodRepository
                .findLastRegisteredByUserId(command.userId())
                .filter(paymentMethod -> paymentMethod.isActive())
                .isPresent();

        if (!hasValidMethod) {
            return new PaymentResult("", false, "Sin método de pago válido");
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
