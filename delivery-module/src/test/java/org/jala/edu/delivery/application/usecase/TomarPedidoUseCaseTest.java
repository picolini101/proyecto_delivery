package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;

class TomarPedidoUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final TomarPedidoUseCase tomarPedido = new TomarPedidoUseCase(orders, new FallbackUserService());

    @Test
    void assignsTheOrderAndRemovesItFromTheReadyList() {
        // Arrange
        UUID orderId = firstReadyOrder();
        TomarPedidoCommand command = new TomarPedidoCommand(orderId, SampleUsers.COURIER);

        // Act
        tomarPedido.execute(command);

        // Assert
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).doesNotContain(orderId);
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourierAndLeavesTheOrderReady() {
        // Arrange
        UUID orderId = firstReadyOrder();
        TomarPedidoCommand command = new TomarPedidoCommand(orderId, SampleUsers.CUSTOMER);

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(orderId);
    }

    @Test
    void rejectsAnOrderAnotherCourierAlreadyTook() {
        // Arrange
        UUID orderId = firstReadyOrder();
        orders.assign(orderId, SampleUsers.COURIER);
        TomarPedidoCommand command = new TomarPedidoCommand(orderId, SampleUsers.COURIER);

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsAnUnknownOrder() {
        // Arrange
        TomarPedidoCommand command = new TomarPedidoCommand(UUID.randomUUID(), SampleUsers.COURIER);

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    private UUID firstReadyOrder() {
        return orders.findReadyForDelivery().getFirst().orderId();
    }
}
