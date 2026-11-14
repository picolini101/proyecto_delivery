package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.ConfirmationCode;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.junit.jupiter.api.Test;

class TomarPedidoUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryActiveDeliveryAdapter activeDeliveries = new InMemoryActiveDeliveryAdapter();
    private final ConfirmationCode code = new ConfirmationCode("4821");
    private final TomarPedidoUseCase tomarPedido = new TomarPedidoUseCase(orders, new FallbackUserService(),
            new FallbackCurrentUserService(UserRole.COURIER), activeDeliveries, () -> code);

    @Test
    void takesAReadyOrderAsOnTheWayAndKeepsTheConfirmationCode() {
        // Arrange
        UUID orderId = firstReadyOrder();

        // Act
        ActiveDelivery delivery = tomarPedido.execute(new TomarPedidoCommand(orderId));

        // Assert
        assertThat(delivery.orderId()).isEqualTo(orderId);
        assertThat(delivery.courierId()).isEqualTo(SampleUsers.COURIER);
        assertThat(delivery.customerId()).isEqualTo(SampleUsers.CUSTOMER);
        assertThat(delivery.step()).isEqualTo(DeliveryStep.ON_THE_WAY);
        assertThat(delivery.confirmationCode()).isEqualTo(code);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).contains(delivery);
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).doesNotContain(orderId);
        assertThat(catchThrowable(() -> orders.markOnTheWay(orderId))).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsASecondDeliveryWhileOneIsInProgress() {
        // Arrange
        UUID first = firstReadyOrder();
        tomarPedido.execute(new TomarPedidoCommand(first));
        UUID second = orders.findReadyForDelivery().getFirst().orderId();

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(new TomarPedidoCommand(second)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .hasMessage("The courier already has a delivery in progress");
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(second);
    }

    @Test
    void rejectsAnOrderAnotherCourierAlreadyTook() {
        // Arrange
        UUID orderId = firstReadyOrder();
        orders.assign(orderId, UUID.randomUUID());

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(new TomarPedidoCommand(orderId)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.COURIER)).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourierAndLeavesTheOrderReady() {
        // Arrange
        UUID orderId = firstReadyOrder();
        TomarPedidoUseCase asCustomer = new TomarPedidoUseCase(orders, new FallbackUserService(),
                new FallbackCurrentUserService(UserRole.CUSTOMER), activeDeliveries, () -> code);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(new TomarPedidoCommand(orderId)));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(orderId);
        assertThat(activeDeliveries.findInProgressByCourier(SampleUsers.CUSTOMER)).isEmpty();
    }

    @Test
    void rejectsAnUnknownOrder() {
        // Arrange
        TomarPedidoCommand command = new TomarPedidoCommand(UUID.randomUUID());

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    private UUID firstReadyOrder() {
        return orders.findReadyForDelivery().getFirst().orderId();
    }
}