package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.delivery.application.port.in.CancelDeliveryCommand;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;

/**
 * Cancels the courier's shipment in progress and cancels the order.
 */
public class CancelDeliveryUseCase implements UseCase<CancelDeliveryCommand, ActiveDelivery> {

    private final OrderService orderService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public CancelDeliveryUseCase(OrderService orderService, UserService userService,
                                  CurrentUserService currentUserService, ActiveDeliveryPort activeDeliveries) {
        this.orderService = orderService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public ActiveDelivery execute(CancelDeliveryCommand command) {
        ActiveDelivery current = currentShipment();
        orderService.cancel(current.orderId());
        ActiveDelivery cancelled = current.withStep(DeliveryStep.CANCELLED);
        activeDeliveries.update(cancelled);
        return cancelled;
    }

    private ActiveDelivery currentShipment() {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        return activeDeliveries.findInProgressByCourier(courierId)
                .orElseThrow(() -> new NotFoundException("The courier has no delivery in progress"));
    }
}
