package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.domain.model.OrderStatus;

/**
 * Caso de uso "Registrar el intento de pago fallido": que hacer cuando el servicio de pagos rechaza el pago.
 * <p>
 * Version del ciclo 1: solo cancela el pedido. Para eso reutiliza {@link ChangeOrderStatusUseCase},
 * que ya valida la transicion y guarda el historial con el motivo.
 */
public class RegisterFailedPaymentUseCase implements UseCase<RegisterFailedPaymentCommand, Void> {

    /** Motivo automatico que queda en el historial del pedido. */
    static final String REASON = "Pago rechazado";

    /** Caso de uso que ya existe para cambiar el estado de un pedido. */
    private final ChangeOrderStatusUseCase changeOrderStatus;

    /**
     * @param changeOrderStatus caso de uso que cambia el estado y guarda el historial
     */
    public RegisterFailedPaymentUseCase(ChangeOrderStatusUseCase changeOrderStatus) {
        this.changeOrderStatus = changeOrderStatus;
    }

    @Override
    public Void execute(RegisterFailedPaymentCommand command) {
        // Cancelar el pedido con el motivo automatico
        changeOrderStatus.execute(new ChangeOrderStatusCommand(
                command.attempt().orderId(),
                OrderStatus.CANCELLED,
                command.customerId(),
                REASON));
        return null;
    }
}