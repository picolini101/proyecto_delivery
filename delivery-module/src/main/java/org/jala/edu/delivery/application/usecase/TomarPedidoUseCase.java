package org.jala.edu.delivery.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;

/**
 * Assigns a ready order to an active courier through {@link OrderService}.
 * An unknown order or a step out of order is reported by that contract.
 */
public class TomarPedidoUseCase implements UseCase<TomarPedidoCommand, Void> {

    private final OrderService orderService;
    private final UserService userService;

    public TomarPedidoUseCase(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @Override
    public Void execute(TomarPedidoCommand command) {
        if (!userService.isActiveWithRole(command.courierId(), UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        orderService.assign(command.orderId(), command.courierId());
        return null;
    }
}
