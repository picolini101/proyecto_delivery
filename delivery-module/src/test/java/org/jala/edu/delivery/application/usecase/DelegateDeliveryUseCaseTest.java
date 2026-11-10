package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.DelegateDeliveryCommand;
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.port.in.StartDeliveryCommand;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class DelegateDeliveryUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final FallbackUserService users = new FallbackUserService();
    private final FallbackCurrentUserService courier = new FallbackCurrentUserService(UserRole.COURIER);
    private final StartDeliveryUseCase startDelivery = new StartDeliveryUseCase(orders, users, courier,
            activeDeliveries, () -> new ConfirmationCode("4821"));
    private final PickUpDeliveryUseCase pickUp = new PickUpDeliveryUseCase(users, courier, activeDeliveries);
    private final DelegateDeliveryUseCase delegate = new DelegateDeliveryUseCase(orders, users, courier,
            activeDeliveries);

    @Test
    void returnsTheOrderSoAnotherCourierCanTakeIt() {
        // Arrange
        UUID orderId = startAssignedOrder();

        // Act
        ActiveDelivery delivery = delegate.execute(new DelegateDeliveryCommand());

        // Assert
        assertThat(delivery.step()).isEqualTo(DeliveryStep.DELEGATED);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).isEmpty();
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(orderId);
        orders.assign(orderId, UUID.randomUUID());
    }

    @Test
    void returnsAShipmentThatWasAlreadyPickedUp() {
        // Arrange
        UUID orderId = startAssignedOrder();
        pickUp.execute(new PickUpDeliveryCommand());

        // Act
        ActiveDelivery delivery = delegate.execute(new DelegateDeliveryCommand());

        // Assert
        assertThat(delivery.step()).isEqualTo(DeliveryStep.DELEGATED);
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(orderId);
    }

    @Test
    void rejectsWhenTheCourierHasNoDeliveryInProgress() {
        // Act
        Throwable thrown = catchThrowable(() -> delegate.execute(new DelegateDeliveryCommand()));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .hasMessage("The courier has no delivery in progress");
    }

    private UUID startAssignedOrder() {
        UUID orderId = orders.findReadyForDelivery().getFirst().orderId();
        startDelivery.execute(new StartDeliveryCommand(orderId));
        return orderId;
    }
}
