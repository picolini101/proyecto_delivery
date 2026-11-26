package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class MarkOrderDeliveredUseCaseTest {
    private MarkOrderDeliveredUseCase useCase;
    private OrderRepositoryPort orderRepository;
    private OrderStatusHistoryPort statusHistory;
    private final UUID adminUserId = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        ChangeOrderStatusUseCase changeStatusUseCase = new ChangeOrderStatusUseCase(
                orderRepository, statusHistory, TransactionRunner.direct());
        useCase = new MarkOrderDeliveredUseCase(changeStatusUseCase);
    }
    @Test
    @DisplayName("Should mark order as DELIVERED")
    void shouldMarkOrderAsDelivered() {

        Order order = createOrderWithStatus(OrderStatus.ON_THE_WAY);
        MarkOrderCommand command = new MarkOrderCommand(order.getId().value(), adminUserId);
        useCase.execute(command);

        Order updated = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getPreviousStatus()).isEqualTo(OrderStatus.ON_THE_WAY);
        assertThat(history.get(0).getNewStatus()).isEqualTo(OrderStatus.DELIVERED);
    }
    @Test
    @DisplayName("Should reject marking an order as DELIVERED when it is not ON_THE_WAY")
    void shouldRejectOrderThatIsNotOnTheWay() {
        Order order = createOrderWithStatus(OrderStatus.ASSIGNED);
        MarkOrderCommand command = new MarkOrderCommand(
                order.getId().value(), adminUserId);
        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(InvalidStateTransitionException.class);
        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(statusHistory.findByOrderId(order.getId())).isEmpty();
    }
    private Order createOrderWithStatus(OrderStatus status) {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Producto Test",
                Money.of(new BigDecimal("25.00")),
                Quantity.of(1));
        Order order = Order.create(
                CustomerId.of(UUID.randomUUID()),
                List.of(line),
                line.getSubtotal());
        if (status == OrderStatus.CONFIRMED
                || status == OrderStatus.READY_FOR_DELIVERY
                || status == OrderStatus.ASSIGNED
                || status == OrderStatus.ON_THE_WAY) {
            order.transitionTo(OrderStatus.CONFIRMED, null);
        }
        if (status == OrderStatus.READY_FOR_DELIVERY ||
                status == OrderStatus.ASSIGNED || status
                == OrderStatus.ON_THE_WAY) {
            order.transitionTo(OrderStatus.READY_FOR_DELIVERY, null);
        }
        if (status == OrderStatus.ASSIGNED || status == OrderStatus.ON_THE_WAY) {
            order.transitionTo(OrderStatus.ASSIGNED, null);
        }
        if (status == OrderStatus.ON_THE_WAY) {
            order.transitionTo(OrderStatus.ON_THE_WAY, null);
        }

        return orderRepository.save(order);
    }
}
