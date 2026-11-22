package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MarkOrderOnTheWayUseCaseTest {
    private MarkOrderOnTheWayUseCase useCase;
    private OrderRepositoryPort orderRepository;
    private OrderStatusHistoryPort statusHistory;
    private final UUID adminUserId = UUID.randomUUID();

    @Test
    @DisplayName("Should mark assigned order as ON_THE_WAY")
    void shouldMarkAssignedOrderAsOnTheWay() {
        //Arrage
        Order order = createOrderWithStatus(OrderStatus.ASSIGNED);
        MarkOrderOnTheWayCommand command = new MarkOrderOnTheWayCommand(order.getId().value(), adminUserId);
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
