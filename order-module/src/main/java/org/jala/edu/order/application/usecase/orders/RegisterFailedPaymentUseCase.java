package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.application.port.out.FailedPaymentLogPort;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.PaymentAttemptResult;

/**
 * Caso de uso "Registrar el intento de pago fallido": que hacer cuando el servicio de pagos rechaza el pago.
 * <p>
 * Version del ciclo 2: cancela el pedido y registra el intento fallido para auditoria.
 * Para cancelar reutiliza {@link ChangeOrderStatusUseCase}, que ya valida la transicion y guarda el historial.
 */
public class RegisterFailedPaymentUseCase implements UseCase<RegisterFailedPaymentCommand, Void> {

    /** Motivo automatico que queda en el historial del pedido. */
    static final String REASON = "Pago rechazado";

    /** Caso de uso que ya existe para cambiar el estado de un pedido. */
    private final ChangeOrderStatusUseCase changeOrderStatus;

    /** Donde se guardan los intentos de pago fallidos (auditoria). */
    private final FailedPaymentLogPort failedPayments;

    /**
     * @param changeOrderStatus caso de uso que cambia el estado y guarda el historial
     * @param failedPayments    puerto donde se guarda el registro de auditoria
     */
    public RegisterFailedPaymentUseCase(ChangeOrderStatusUseCase changeOrderStatus,
                                        FailedPaymentLogPort failedPayments) {
        this.changeOrderStatus = changeOrderStatus;
        this.failedPayments = failedPayments;
    }

    @Override
    public Void execute(RegisterFailedPaymentCommand command) {
        // 1. Cancelar el pedido con el motivo automatico
        changeOrderStatus.execute(new ChangeOrderStatusCommand(
                command.attempt().orderId(),
                OrderStatus.CANCELLED,
                command.customerId(),
                REASON));

        // 2. Registrar el intento fallido (resultado, motivo; la fecha y hora las pone el registro)
        failedPayments.save(FailedPaymentRecord.record(
                OrderId.of(command.attempt().orderId()),
                command.customerId(),
                PaymentAttemptResult.REJECTED,
                command.attempt().message()));
        return null;
    }
}