package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.out.CourierAvailabilityPort;
import org.jala.edu.delivery.domain.exception.CourierNotAvailableException;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;

/**
 * Checks that a courier can take an order: they must be an active courier and be available.
 * Returns normally when the courier is eligible and throws otherwise.
 */
public class ValidateCourierEligibilityUseCase implements UseCase<UUID, Void> {

    private final UserService userService;
    private final CourierAvailabilityPort availability;

    public ValidateCourierEligibilityUseCase(UserService userService, CourierAvailabilityPort availability) {
        this.userService = userService;
        this.availability = availability;
    }

    @Override
    public Void execute(UUID courierId) {
        if (!userService.isActiveWithRole(courierId, UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        if (!availability.isAvailable(courierId)) {
            throw new CourierNotAvailableException();
        }
        return null;
    }
}
