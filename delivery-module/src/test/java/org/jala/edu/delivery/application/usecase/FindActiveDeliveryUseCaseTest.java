package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.FindActiveDeliveryQuery;
import org.jala.edu.delivery.application.port.in.StartDeliveryCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class FindActiveDeliveryUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final FallbackUserService users = new FallbackUserService();
    private final FallbackCurrentUserService courier = new FallbackCurrentUserService(UserRole.COURIER);
    private final StartDeliveryUseCase startDelivery = new StartDeliveryUseCase(orders, users, courier,
            activeDeliveries, () -> new ConfirmationCode("4821"));
    private final FindActiveDeliveryUseCase find = new FindActiveDeliveryUseCase(users, courier, activeDeliveries);

    @Test
    void returnsTheShipmentInProgress() {
        // Arrange
        UUID orderId = orders.findReadyForDelivery().getFirst().orderId();
        orders.assign(orderId, SampleUsers.COURIER);
        startDelivery.execute(new StartDeliveryCommand(orderId));

        // Act
        Optional<ActiveDelivery> delivery = find.execute(new FindActiveDeliveryQuery());

        // Assert
        assertThat(delivery).isPresent();
        assertThat(delivery.orElseThrow().orderId()).isEqualTo(orderId);
        assertThat(delivery.orElseThrow().step()).isEqualTo(DeliveryStep.ON_THE_WAY);
    }

    @Test
    void returnsEmptyWhenTheCourierHasNoShipmentInProgress() {
        // Act
        Optional<ActiveDelivery> delivery = find.execute(new FindActiveDeliveryQuery());

        // Assert
        assertThat(delivery).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourier() {
        // Arrange
        FindActiveDeliveryUseCase asCustomer = new FindActiveDeliveryUseCase(users,
                new FallbackCurrentUserService(UserRole.CUSTOMER), activeDeliveries);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(new FindActiveDeliveryQuery()));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class);
    }
}
