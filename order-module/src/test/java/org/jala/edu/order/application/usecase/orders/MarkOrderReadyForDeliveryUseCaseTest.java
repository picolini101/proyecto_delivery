package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.application.port.in.MarkOrderReadyForDeliveryCommand;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkOrderReadyForDeliveryUseCaseTest {

    private final InMemoryOrderRepositoryAdapter orderRepository = new InMemoryOrderRepositoryAdapter();
    private final InMemoryOrderStatusHistoryAdapter statusHistory = new InMemoryOrderStatusHistoryAdapter();
    private final UUID merchantId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private MarkOrderReadyForDeliveryUseCase useCase;

    @BeforeEach
    void setUp() {
        ChangeOrderStatusUseCase changeOrderStatus = new ChangeOrderStatusUseCase(
                orderRepository, statusHistory, TransactionRunner.direct());
        useCase = new MarkOrderReadyForDeliveryUseCase(orderRepository, changeOrderStatus);
    }

    @Test
    void marksConfirmedOrderReadyAndRecordsHistory() {
        Order order = confirmedOrder(merchantId);

        useCase.execute(new MarkOrderReadyForDeliveryCommand(order.getId().value(), merchantId, userId));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.READY_FOR_DELIVERY);
        assertThat(statusHistory.findByOrderId(order.getId()))
                .singleElement()
                .satisfies(change -> {
                    assertThat(change.getPreviousStatus()).isEqualTo(OrderStatus.CONFIRMED);
                    assertThat(change.getNewStatus()).isEqualTo(OrderStatus.READY_FOR_DELIVERY);
                    assertThat(change.getChangedById()).isEqualTo(userId);
                    assertThat(change.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void rejectsOrderOwnedByAnotherMerchantWithoutChangingIt() {
        Order order = confirmedOrder(UUID.randomUUID());

        assertThatThrownBy(() -> useCase.execute(
                new MarkOrderReadyForDeliveryCommand(order.getId().value(), merchantId, userId)))
                .isInstanceOf(UnauthorizedCustomerException.class);

        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(statusHistory.findByOrderId(order.getId())).isEmpty();
    }

    private Order confirmedOrder(UUID commerceId) {
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), commerceId, "Product",
                Money.of(10.0), Quantity.of(1));
        Order order = Order.create(CustomerId.of(UUID.randomUUID()), List.of(line), line.getSubtotal());
        order.transitionTo(OrderStatus.CONFIRMED, null);
        return orderRepository.save(order);
    }
}
