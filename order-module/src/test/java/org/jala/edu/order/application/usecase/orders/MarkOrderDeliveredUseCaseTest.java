package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.order.application.port.in.MarkOrderCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.*;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;


public class MarkOrderDeliveredUseCaseTest {
    private MarkOrderDeliveredUseCase useCase;
    private OrderRepositoryPort orderRepository;
    private OrderStatusHistoryPort statusHistory;
    private final UUID adminUserId = UUID.randomUUID();
    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        ChangeOrderStatusUseCase changeStatusUseCase = new ChangeOrderStatusUseCase(orderRepository, statusHistory, TransactionRunner.direct());
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
        if (status == OrderStatus.ON_THE_WAY) {
            order.transitionTo(OrderStatus.ON_THE_WAY, null);
        }
        return orderRepository.save(order);
    }
}
