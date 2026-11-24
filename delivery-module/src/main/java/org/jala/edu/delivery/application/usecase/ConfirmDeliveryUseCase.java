package org.jala.edu.delivery.application.usecase;

import java.util.Objects;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.delivery.application.port.in.ConfirmDeliveryCommand;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.InvalidConfirmationCodeException;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;

/**
 * Closes a picked-up shipment when the confirmation code matches, and marks the order delivered.
 */
public class ConfirmDeliveryUseCase implements UseCase<ConfirmDeliveryCommand, ActiveDelivery> {

    private final OrderService orderService;
    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public ConfirmDeliveryUseCase(OrderService orderService, UserService userService,
                                   CurrentUserService currentUserService, ActiveDeliveryPort activeDeliveries) {
        this.orderService = orderService;
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public ActiveDelivery execute(ConfirmDeliveryCommand command) {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        ActiveDelivery current = activeDeliveries.findInProgressByCourier(courierId)
                .orElseThrow(() -> new NotFoundException("The courier has no delivery in progress"));
        if (current.step() != DeliveryStep.PICKED_UP) {
            throw new ConflictException("Delivery is " + current.step() + ", expected PICKED_UP");
        }
        if (!Objects.equals(command.confirmationCode(), current.confirmationCode().value())) {
            throw new InvalidConfirmationCodeException();
        }
        orderService.markDelivered(current.orderId());
        ActiveDelivery delivered = current.withStep(DeliveryStep.DELIVERED);
        activeDeliveries.update(delivered);
        return delivered;
    }
}
