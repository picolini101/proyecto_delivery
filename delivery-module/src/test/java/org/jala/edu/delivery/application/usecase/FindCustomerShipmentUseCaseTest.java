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
import org.jala.edu.delivery.application.port.in.CancelDeliveryCommand;
import org.jala.edu.delivery.application.port.in.FindCustomerShipmentQuery;
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.port.in.StartDeliveryCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class FindCustomerShipmentUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final FallbackUserService users = new FallbackUserService();
    private final FallbackCurrentUserService courier = new FallbackCurrentUserService(UserRole.COURIER);
    private final FallbackCurrentUserService customer = new FallbackCurrentUserService(UserRole.CUSTOMER);
    private final ConfirmationCode code = new ConfirmationCode("4821");
    private final StartDeliveryUseCase startDelivery = new StartDeliveryUseCase(orders, users, courier,
            activeDeliveries, () -> code);
    private final PickUpDeliveryUseCase pickUp = new PickUpDeliveryUseCase(users, courier, activeDeliveries);
    private final CancelDeliveryUseCase cancel = new CancelDeliveryUseCase(orders, users, courier, activeDeliveries);
    private final FindCustomerShipmentUseCase find = new FindCustomerShipmentUseCase(users, customer,
            activeDeliveries);

    @Test
    void returnsTheSameStepAndConfirmationCodeTheCourierIsCarrying() {
        // Arrange
        UUID orderId = startFirstOrder();

        // Act
        Optional<ActiveDelivery> delivery = find.execute(new FindCustomerShipmentQuery());
        pickUp.execute(new PickUpDeliveryCommand());
        Optional<ActiveDelivery> pickedUp = find.execute(new FindCustomerShipmentQuery());

        // Assert
        assertThat(delivery).isPresent();
        assertThat(delivery.orElseThrow().orderId()).isEqualTo(orderId);
        assertThat(delivery.orElseThrow().step()).isEqualTo(DeliveryStep.ON_THE_WAY);
        assertThat(delivery.orElseThrow().confirmationCode()).isEqualTo(code);
        assertThat(pickedUp.orElseThrow().step()).isEqualTo(DeliveryStep.PICKED_UP);
        assertThat(pickedUp.orElseThrow().confirmationCode()).isEqualTo(code);
    }

    @Test
    void keepsACancelledShipmentVisibleAsCancelled() {
        // Arrange
        startFirstOrder();
        cancel.execute(new CancelDeliveryCommand());

        // Act
        Optional<ActiveDelivery> delivery = find.execute(new FindCustomerShipmentQuery());

        // Assert
        assertThat(delivery.orElseThrow().step()).isEqualTo(DeliveryStep.CANCELLED);
    }

    @Test
    void returnsEmptyWhenTheCustomerHasNoShipment() {
        // Act
        Optional<ActiveDelivery> delivery = find.execute(new FindCustomerShipmentQuery());

        // Assert
        assertThat(delivery).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCustomer() {
        // Arrange
        FindCustomerShipmentUseCase asCourier = new FindCustomerShipmentUseCase(users, courier, activeDeliveries);

        // Act
        Throwable thrown = catchThrowable(() -> asCourier.execute(new FindCustomerShipmentQuery()));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class);
    }

    private UUID startFirstOrder() {
        UUID orderId = orders.findReadyForDelivery().getFirst().orderId();
        orders.assign(orderId, SampleUsers.COURIER);
        startDelivery.execute(new StartDeliveryCommand(orderId));
        return orderId;
    }
}
