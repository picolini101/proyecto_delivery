package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MarkOrderAssignedUseCaseTest {
    private MarkOrderAssignedUseCase useCase;
    private OrderRepositoryPort orderRepository;
    private final UUID courierId = UUID.randomUUID();
    private final UUID adminUserId = UUID.randomUUID();
    private OrderStatusHistoryPort statusHistory;
    @Test
    @DisplayName("Should assign courier and change order status to ASSIGNED")
    void shouldAssignCourierSuccessfully() {
        Order order = createReadyOrder();
        AssignedOrderCommand command = new AssignedOrderCommand(
                order.getId().value(),
                courierId,
                adminUserId);
        useCase.execute(command);
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(updated.getCourierId()).isEqualTo(courierId);
        assertThat(statusHistory.findByOrderId(order.getId())).hasSize(1);
    }
    private Order createReadyOrder() {
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
        order.transitionTo(OrderStatus.CONFIRMED, null);
        order.transitionTo(OrderStatus.READY_FOR_DELIVERY, null);
        return orderRepository.save(order);
    }
}
