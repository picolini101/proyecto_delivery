package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.StartDeliveryCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class StartDeliveryUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final ConfirmationCode code = new ConfirmationCode("4821");
    private final StartDeliveryUseCase startDelivery = new StartDeliveryUseCase(orders, new FallbackUserService(),
            new FallbackCurrentUserService(UserRole.COURIER), activeDeliveries, () -> code);

    @Test
    void startsAnAssignedOrderAsOnTheWayAndKeepsTheConfirmationCode() {
        // Arrange
        UUID orderId = firstReadyOrder();
        orders.assign(orderId, SampleUsers.COURIER);

        // Act
        ActiveDelivery delivery = startDelivery.execute(new StartDeliveryCommand(orderId));

        // Assert
        assertThat(delivery.orderId()).isEqualTo(orderId);
        assertThat(delivery.courierId()).isEqualTo(SampleUsers.COURIER);
        assertThat(delivery.customerId()).isEqualTo(SampleUsers.CUSTOMER);
        assertThat(delivery.step()).isEqualTo(DeliveryStep.ON_THE_WAY);
        assertThat(delivery.confirmationCode()).isEqualTo(code);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).contains(delivery);
        assertThat(catchThrowable(() -> orders.markOnTheWay(orderId))).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsASecondDeliveryWhileOneIsInProgress() {
        // Arrange
        UUID first = firstReadyOrder();
        orders.assign(first, SampleUsers.COURIER);
        startDelivery.execute(new StartDeliveryCommand(first));
        UUID second = orders.findReadyForDelivery().getFirst().orderId();
        orders.assign(second, SampleUsers.COURIER);

        // Act
        Throwable thrown = catchThrowable(() -> startDelivery.execute(new StartDeliveryCommand(second)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .hasMessage("The courier already has a delivery in progress");
        orders.markOnTheWay(second);
    }

    @Test
    void rejectsAnOrderThatWasNotAssignedAndStoresNothing() {
        // Arrange
        UUID orderId = firstReadyOrder();

        // Act
        Throwable thrown = catchThrowable(() -> startDelivery.execute(new StartDeliveryCommand(orderId)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourier() {
        // Arrange
        UUID orderId = firstReadyOrder();
        orders.assign(orderId, SampleUsers.CUSTOMER);
        StartDeliveryUseCase asCustomer = new StartDeliveryUseCase(orders, new FallbackUserService(),
                new FallbackCurrentUserService(UserRole.CUSTOMER), activeDeliveries, () -> code);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(new StartDeliveryCommand(orderId)));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.CUSTOMER)).isEmpty();
    }

    private UUID firstReadyOrder() {
        return orders.findReadyForDelivery().getFirst().orderId();
    }
}
