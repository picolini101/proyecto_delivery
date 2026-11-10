package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;

/**
 * Advances the courier's shipment from on the way to picked up.
 * The order stays on the way until the courier confirms the delivery with the code.
 */
public class PickUpDeliveryUseCase implements UseCase<PickUpDeliveryCommand, ActiveDelivery> {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public PickUpDeliveryUseCase(UserService userService, CurrentUserService currentUserService,
                                  ActiveDeliveryPort activeDeliveries) {
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public ActiveDelivery execute(PickUpDeliveryCommand command) {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        ActiveDelivery current = activeDeliveries.findInProgressByCourier(courierId)
                .orElseThrow(() -> new NotFoundException("The courier has no delivery in progress"));
        if (current.step() != DeliveryStep.ON_THE_WAY) {
            throw new ConflictException("Delivery is " + current.step() + ", expected ON_THE_WAY");
        }
        ActiveDelivery pickedUp = current.withStep(DeliveryStep.PICKED_UP);
        activeDeliveries.update(pickedUp);
        return pickedUp;
    }
}
