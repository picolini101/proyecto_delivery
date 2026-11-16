package org.jala.edu.delivery.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.application.port.out.CourierAvailabilityPort;

/**
 * Assigns a ready order to the signed-in courier through {@link OrderService}.
 * The courier must be eligible first (active and available); once the order is assigned they are busy, so they are
 * marked as not available. An unknown order or a step out of order is reported by that contract.
 */
public class TomarPedidoUseCase implements UseCase<TomarPedidoCommand, Void> {

    private final OrderService orderService;
    private final ValidateCourierEligibilityUseCase validateEligibility;
    private final CourierAvailabilityPort availability;
    private final CurrentUserService currentUserService;

    public TomarPedidoUseCase(OrderService orderService, ValidateCourierEligibilityUseCase validateEligibility,
                               CourierAvailabilityPort availability, CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.validateEligibility = validateEligibility;
        this.availability = availability;
        this.currentUserService = currentUserService;
    }

    @Override
    public Void execute(TomarPedidoCommand command) {
        UUID courierId = currentUserService.currentUser().id();
        validateEligibility.execute(courierId);
        orderService.assign(command.orderId(), courierId);
        availability.markUnavailable(courierId);
        return null;
    }
}
