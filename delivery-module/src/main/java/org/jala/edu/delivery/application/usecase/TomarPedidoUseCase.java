package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;

/**
 * Assigns a ready order to the signed-in courier through {@link OrderService}.
 * An unknown order or a step out of order is reported by that contract.
 */
public class TomarPedidoUseCase implements UseCase<TomarPedidoCommand, Void> {

    private final OrderService orderService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public TomarPedidoUseCase(OrderService orderService, UserService userService,
                               CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @Override
    public Void execute(TomarPedidoCommand command) {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        orderService.assign(command.orderId(), courierId);
        return null;
    }
}
