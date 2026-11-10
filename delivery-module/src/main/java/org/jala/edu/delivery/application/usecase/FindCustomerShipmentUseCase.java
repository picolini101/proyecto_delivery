package org.jala.edu.delivery.application.usecase;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.FindCustomerShipmentQuery;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;

/**
 * Returns the signed-in customer's shipment, including the confirmation code the courier must match.
 */
public class FindCustomerShipmentUseCase implements UseCase<FindCustomerShipmentQuery, Optional<ActiveDelivery>> {

    private final UserService userService;
    private final CurrentUserService currentUserService;
    private final ActiveDeliveryPort activeDeliveries;

    public FindCustomerShipmentUseCase(UserService userService, CurrentUserService currentUserService,
                                        ActiveDeliveryPort activeDeliveries) {
        this.userService = userService;
        this.currentUserService = currentUserService;
        this.activeDeliveries = activeDeliveries;
    }

    @Override
    public Optional<ActiveDelivery> execute(FindCustomerShipmentQuery query) {
        UUID customerId = currentUserService.currentUser().id();
        if (!userService.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedOperationException();
        }
        return activeDeliveries.findLatestByCustomer(customerId);
    }
}
