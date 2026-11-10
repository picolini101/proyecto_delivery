package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.ConfirmDeliveryCommand;
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.port.in.StartDeliveryCommand;
import org.jala.edu.delivery.domain.exception.InvalidConfirmationCodeException;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class ConfirmDeliveryUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final ConfirmationCode code = new ConfirmationCode("4821");
    private final FallbackUserService users = new FallbackUserService();
    private final FallbackCurrentUserService courier = new FallbackCurrentUserService(UserRole.COURIER);
    private final StartDeliveryUseCase startDelivery = new StartDeliveryUseCase(orders, users, courier,
            activeDeliveries, () -> code);
    private final PickUpDeliveryUseCase pickUp = new PickUpDeliveryUseCase(users, courier, activeDeliveries);
    private final ConfirmDeliveryUseCase confirm = new ConfirmDeliveryUseCase(orders, users, courier,
            activeDeliveries);

    @Test
    void closesTheShipmentWhenTheCodeMatches() {
        // Arrange
        UUID orderId = pickUpAssignedOrder();

        // Act
        ActiveDelivery delivery = confirm.execute(new ConfirmDeliveryCommand("4821"));

        // Assert
        assertThat(delivery.step()).isEqualTo(DeliveryStep.DELIVERED);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).isEmpty();
        assertThat(catchThrowable(() -> orders.markDelivered(orderId))).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsAWrongCodeAndLeavesTheShipmentPickedUp() {
        // Arrange
        UUID orderId = pickUpAssignedOrder();

        // Act
        Throwable thrown = catchThrowable(() -> confirm.execute(new ConfirmDeliveryCommand("0000")));

        // Assert
        assertThat(thrown).isInstanceOf(InvalidConfirmationCodeException.class)
                .hasMessage("The confirmation code does not match");
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER))
                .get()
                .extracting(ActiveDelivery::step)
                .isEqualTo(DeliveryStep.PICKED_UP);
        orders.markDelivered(orderId);
    }

    @Test
    void rejectsADeliveryThatHasNotBeenPickedUp() {
        // Arrange
        UUID orderId = startAssignedOrder();

        // Act
        Throwable thrown = catchThrowable(() -> confirm.execute(new ConfirmDeliveryCommand("4821")));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .hasMessage("Delivery is ON_THE_WAY, expected PICKED_UP");
        orders.markDelivered(orderId);
    }

    @Test
    void rejectsWhenTheCourierHasNoDeliveryInProgress() {
        // Act
        Throwable thrown = catchThrowable(() -> confirm.execute(new ConfirmDeliveryCommand("4821")));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .hasMessage("The courier has no delivery in progress");
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourier() {
        // Arrange
        pickUpAssignedOrder();
        ConfirmDeliveryUseCase asCustomer = new ConfirmDeliveryUseCase(orders, users,
                new FallbackCurrentUserService(UserRole.CUSTOMER), activeDeliveries);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(new ConfirmDeliveryCommand("4821")));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER))
                .get()
                .extracting(ActiveDelivery::step)
                .isEqualTo(DeliveryStep.PICKED_UP);
    }

    private UUID pickUpAssignedOrder() {
        startAssignedOrder();
        pickUp.execute(new PickUpDeliveryCommand());
        return activeDeliveries.findInProgressByCourier(SampleUsers.COURIER).orElseThrow().orderId();
    }

    private UUID startAssignedOrder() {
        UUID orderId = orders.findReadyForDelivery().getFirst().orderId();
        startDelivery.execute(new StartDeliveryCommand(orderId));
        return orderId;
    }
}
