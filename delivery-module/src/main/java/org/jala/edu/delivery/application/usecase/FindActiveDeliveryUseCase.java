package org.jala.edu.delivery.application.usecase;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.FindActiveDeliveryQuery;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;

/**
 * Returns the signed-in courier's shipment in progress, or empty when there is none.
 */
public class FindActiveDeliveryUseCase implements UseCase<FindActiveDeliveryQuery, Optional<ActiveDelivery>> {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public FindActiveDeliveryUseCase(UserService userService, CurrentUserService currentUserService,
                                      ActiveDeliveryPort activeDeliveries) {
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public Optional<ActiveDelivery> execute(FindActiveDeliveryQuery query) {
        UUID courierId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        return activeDeliveries.findInProgressByCourier(courierId);
    }
}
