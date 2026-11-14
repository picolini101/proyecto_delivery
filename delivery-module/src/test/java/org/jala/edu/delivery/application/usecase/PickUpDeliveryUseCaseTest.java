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
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class PickUpDeliveryUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final ConfirmationCode code = new ConfirmationCode("4821");
    private final TomarPedidoUseCase tomarPedido = new TomarPedidoUseCase(orders, new FallbackUserService(),
            new FallbackCurrentUserService(UserRole.COURIER), activeDeliveries, () -> code);
    private final PickUpDeliveryUseCase pickUp = new PickUpDeliveryUseCase(new FallbackUserService(),
            new FallbackCurrentUserService(UserRole.COURIER), activeDeliveries);

    @Test
    void marksTheShipmentAsPickedUpAndLeavesTheOrderOnTheWay() {
        // Arrange
        UUID orderId = startAssignedOrder();

        // Act
        ActiveDelivery delivery = pickUp.execute(new PickUpDeliveryCommand());

        // Assert
        assertThat(delivery.orderId()).isEqualTo(orderId);
        assertThat(delivery.step()).isEqualTo(DeliveryStep.PICKED_UP);
        assertThat(delivery.confirmationCode()).isEqualTo(code);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).contains(delivery);
        orders.markDelivered(orderId);
    }

    @Test
    void rejectsASecondPickUp() {
        // Arrange
        UUID orderId = startAssignedOrder();
        pickUp.execute(new PickUpDeliveryCommand());

        // Act
        Throwable thrown = catchThrowable(() -> pickUp.execute(new PickUpDeliveryCommand()));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .hasMessage("Delivery is PICKED_UP, expected ON_THE_WAY");
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER))
                .get()
                .extracting(ActiveDelivery::step)
                .isEqualTo(DeliveryStep.PICKED_UP);
        orders.markDelivered(orderId);
    }

    @Test
    void rejectsWhenTheCourierHasNoDeliveryInProgress() {
        // Act
        Throwable thrown = catchThrowable(() -> pickUp.execute(new PickUpDeliveryCommand()));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .hasMessage("The courier has no delivery in progress");
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourier() {
        // Arrange
        startAssignedOrder();
        PickUpDeliveryUseCase asCustomer = new PickUpDeliveryUseCase(new FallbackUserService(),
                new FallbackCurrentUserService(UserRole.CUSTOMER), activeDeliveries);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(new PickUpDeliveryCommand()));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER))
                .get()
                .extracting(ActiveDelivery::step)
                .isEqualTo(DeliveryStep.ON_THE_WAY);
    }

    private UUID startAssignedOrder() {
        UUID orderId = orders.findReadyForDelivery().getFirst().orderId();
        tomarPedido.execute(new TomarPedidoCommand(orderId));
        return orderId;
    }
}
