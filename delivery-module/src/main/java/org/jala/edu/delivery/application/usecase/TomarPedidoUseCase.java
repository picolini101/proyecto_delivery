package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.application.port.out.ConfirmationCodeGenerator;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;

/**
 * Takes a ready order for the signed-in courier, moves it to on the way and keeps it as the
 * shipment in progress. The confirmation code is stored now and checked when the courier finishes.
 */
public class TomarPedidoUseCase implements UseCase<TomarPedidoCommand, ActiveDelivery> {

    private final OrderService orderService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;
    private final ConfirmationCodeGenerator confirmationCodes;

    public TomarPedidoUseCase(OrderService orderService, UserService userService,
                               CurrentUserService currentUserService, ActiveDeliveryPort activeDeliveries,
                               ConfirmationCodeGenerator confirmationCodes) {
        this.orderService = orderService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
        this.confirmationCodes = confirmationCodes;
    }

    @Override
    public ActiveDelivery execute(TomarPedidoCommand command) {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        if (activeDeliveries.findInProgressByCourier(courierId).isPresent()) {
            throw new ConflictException("The courier already has a delivery in progress");
        }
        UUID customerId = orderService.customerOf(command.orderId());
        orderService.assign(command.orderId(), courierId);
        orderService.markOnTheWay(command.orderId());
        ActiveDelivery delivery = new ActiveDelivery(command.orderId(), courierId, customerId,
                DeliveryStep.ON_THE_WAY, confirmationCodes.next());
        activeDeliveries.add(delivery);
        return delivery;
    }
}