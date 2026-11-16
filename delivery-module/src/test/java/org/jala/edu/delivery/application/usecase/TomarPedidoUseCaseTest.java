package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierAvailabilityAdapter;
import org.junit.jupiter.api.Test;

class TomarPedidoUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final InMemoryCourierAvailabilityAdapter availability = new InMemoryCourierAvailabilityAdapter();
    private final CurrentUserService signedInCourier = new FallbackCurrentUserService(UserRole.COURIER);
    private final TomarPedidoUseCase tomarPedido = tomarPedidoAs(signedInCourier);

    @Test
    void assignsTheOrderAndRemovesItFromTheReadyList() {
        // Arrange
        UUID orderId = firstReadyOrder();
        TomarPedidoCommand command = new TomarPedidoCommand(orderId);

        // Act
        tomarPedido.execute(command);

        // Assert
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).doesNotContain(orderId);
    }

    @Test
    void theCourierIsNoLongerAvailableAfterTakingAnOrder() {
        // Arrange
        TomarPedidoCommand command = new TomarPedidoCommand(firstReadyOrder());

        // Act
        tomarPedido.execute(command);

        // Assert
        assertThat(availability.isAvailable(courierId())).isFalse();
    }

    @Test
    void rejectsACourierWhoIsBusyWithAnotherOrderAndLeavesTheOrderReady() {
        // Arrange
        tomarPedido.execute(new TomarPedidoCommand(firstReadyOrder()));
        UUID secondOrderId = firstReadyOrder();
        TomarPedidoCommand command = new TomarPedidoCommand(secondOrderId);

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("El repartidor no está disponible para tomar pedidos");
        assertThat(orders.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(secondOrderId);
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourierAndLeavesTheOrderReady() {
        // Arrange
        UUID orderId = firstReadyOrder();
        TomarPedidoUseCase asCustomer = tomarPedidoAs(new FallbackCurrentUserService(UserRole.CUSTOMER));
        TomarPedidoCommand command = new TomarPedidoCommand(orderId);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(command));

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
        TomarPedidoCommand command = new TomarPedidoCommand(orderId);

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsAnUnknownOrderAndKeepsTheCourierAvailable() {
        // Arrange
        TomarPedidoCommand command = new TomarPedidoCommand(UUID.randomUUID());

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertThat(availability.isAvailable(courierId())).isTrue();
    }

    private TomarPedidoUseCase tomarPedidoAs(CurrentUserService currentUser) {
        FallbackUserService users = new FallbackUserService();
        return new TomarPedidoUseCase(orders, new ValidateCourierEligibilityUseCase(users, availability), availability,
                currentUser);
    }

    private UUID courierId() {
        return signedInCourier.currentUser().id();
    }

    private UUID firstReadyOrder() {
        return orders.findReadyForDelivery().getFirst().orderId();
    }
}
