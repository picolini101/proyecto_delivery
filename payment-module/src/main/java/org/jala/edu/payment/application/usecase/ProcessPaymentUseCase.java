package org.jala.edu.payment.application.usecase;

import java.math.BigDecimal;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionPort;

public class ProcessPaymentUseCase {

    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("5000.00");

    private final UserService userService;
    private final PaymentMethodValidationPort paymentMethodValidationPort;
    private final PaymentTransactionPort paymentTransactionPort;

    public ProcessPaymentUseCase(
            UserService userService,
            PaymentMethodValidationPort paymentMethodValidationPort,
            PaymentTransactionPort paymentTransactionPort) {

        this.userService = userService;
        this.paymentMethodValidationPort = paymentMethodValidationPort;
        this.paymentTransactionPort = paymentTransactionPort;
    }

    public PaymentResult execute(ProcessPaymentCommand command) {

        if (command == null) {
            return reject(null, "La solicitud de pago es obligatoria");
        }

        if (command.orderId() == null) {
            return reject(command, "El pedido es obligatorio");
        }

        if (command.userId() == null) {
            return reject(command, "El usuario es obligatorio");
        }

        BigDecimal amount = command.amount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return reject(command, "El monto debe ser mayor a cero");
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            return reject(command, "El monto excede el limite permitido");
        }

        if (!userService.isActiveWithRole(
                command.userId(), UserRole.CUSTOMER)) {
            return new PaymentResult(
                    "",
                    false,
                    "Usuario no autorizado para realizar pagos"
            );
        }
        if (command.paymentMethodId() == null) {
            return reject(command, "El metodo de pago es obligatorio");
        }

        if (!paymentMethodValidationPort.isValidPaymentMethod(
                command.userId(),
                command.paymentMethodId())) {
            return reject(command, "Metodo de pago invalido o inactivo");
        }
        String reference = paymentTransactionPort.recordApproved(
                command.orderId(),
                command.userId(),
                command.paymentMethodId(),
                amount
        );

        return new PaymentResult(reference, true, "");
    }

    private PaymentResult reject(
            ProcessPaymentCommand command,
            String reason) {

        if (command != null) {
            paymentTransactionPort.recordRejected(
                    command.orderId(),
                    command.userId(),
                    command.paymentMethodId(),
                    command.amount(),
                    reason
            );
        }

        return new PaymentResult("", false, reason);
    }
}
