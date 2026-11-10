package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.delivery.application.port.in.DelegateDeliveryCommand;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;

/**
 * Hands the courier's shipment back. The order is ready for another courier.
 */
public class DelegateDeliveryUseCase implements UseCase<DelegateDeliveryCommand, ActiveDelivery> {

    private final OrderService orderService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public DelegateDeliveryUseCase(OrderService orderService, UserService userService,
                                    CurrentUserService currentUserService, ActiveDeliveryPort activeDeliveries) {
        this.orderService = orderService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public ActiveDelivery execute(DelegateDeliveryCommand command) {
        ActiveDelivery current = currentShipment();
        orderService.release(current.orderId());
        ActiveDelivery delegated = current.withStep(DeliveryStep.DELEGATED);
        activeDeliveries.update(delegated);
        return delegated;
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
