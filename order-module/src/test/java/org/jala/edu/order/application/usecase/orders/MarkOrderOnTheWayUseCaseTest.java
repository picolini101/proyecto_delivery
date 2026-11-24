package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.in.MarkOrderCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MarkOrderOnTheWayUseCaseTest {
    private MarkOrderOnTheWayUseCase useCase;
    private OrderRepositoryPort orderRepository;
    private OrderStatusHistoryPort statusHistory;
    private final UUID adminUserId = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        ChangeOrderStatusUseCase changeStatusUseCase =
                new ChangeOrderStatusUseCase(
                        orderRepository,
                        statusHistory,
                        TransactionRunner.direct());
        useCase = new MarkOrderOnTheWayUseCase(changeStatusUseCase);
    }
    @Test
    @DisplayName("Should mark assigned order as ON_THE_WAY")
    void shouldMarkAssignedOrderAsOnTheWay() {
        //Arrage
        Order order = createOrderWithStatus(OrderStatus.ASSIGNED);
        MarkOrderCommand command = new MarkOrderCommand(order.getId().value(), adminUserId);
        //act
        useCase.execute(command);
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        //assert
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.ON_THE_WAY);
        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getPreviousStatus())
                .isEqualTo(OrderStatus.ASSIGNED);
        assertThat(history.get(0).getNewStatus())
                .isEqualTo(OrderStatus.ON_THE_WAY);
    }
    @Test
    @DisplayName("Should reject marking an order as ON_THE_WAY when it is not ASSIGNED")
    void shouldRejectOrderThatIsNotAssigned() {
        Order order = createOrderWithStatus(OrderStatus.READY_FOR_DELIVERY);
        MarkOrderCommand command = new MarkOrderCommand(
                        order.getId().value(), adminUserId);
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidStateTransitionException.class);
        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.READY_FOR_DELIVERY);
        assertThat(statusHistory.findByOrderId(order.getId())).isEmpty();
    }
    @Test
    @DisplayName("Should throw NotFoundException when order does not exist")
    void shouldThrowNotFoundExceptionWhenOrderDoesNotExist() {
        MarkOrderCommand command = new MarkOrderCommand(
                        UUID.randomUUID(),
                        adminUserId);
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(NotFoundException.class);
    }
    private Order createOrderWithStatus(OrderStatus status) {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Producto Test",
                Money.of(new BigDecimal("25.00")),
                Quantity.of(1));
        Order order = Order.create(CustomerId.of(UUID.randomUUID()), List.of(line), line.getSubtotal());
        if (status == OrderStatus.CONFIRMED || status == OrderStatus.READY_FOR_DELIVERY || status == OrderStatus.ASSIGNED) {
            order.transitionTo(OrderStatus.CONFIRMED, null);
        }
        if (status == OrderStatus.READY_FOR_DELIVERY || status == OrderStatus.ASSIGNED) {
            order.transitionTo(OrderStatus.READY_FOR_DELIVERY, null);
        }
        if (status == OrderStatus.ASSIGNED) {
            order.transitionTo(OrderStatus.ASSIGNED, null);
        }
        return orderRepository.save(order);
    }
}
