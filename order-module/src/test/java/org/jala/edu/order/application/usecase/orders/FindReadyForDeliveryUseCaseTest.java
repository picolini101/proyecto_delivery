
package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;

import org.jala.edu.core.contract.ReadyOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FindReadyForDeliveryUseCaseTest {
    private OrderRepositoryPort orderRepository;
    private FindReadyForDeliveryUseCase useCase;
    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepositoryPort.class);
        useCase = new FindReadyForDeliveryUseCase(orderRepository);
    }
    @Test
    @DisplayName("Should return ready orders with their delivery information")
    void shouldReturnReadyOrdersWithDeliveryInformation() {
        UUID orderId = UUID.randomUUID();
        DeliveryAddress address = new DeliveryAddress("Cochabamba", "Av. América", "123");
        Money shippingCost = Money.of(new BigDecimal("10.00"));
        Order order = Order.restore(
                OrderId.of(orderId),
                CustomerId.of(UUID.randomUUID()),
                List.of(OrderLine.of(
                                ProductId.of(UUID.randomUUID()),
                                UUID.randomUUID(),
                                "Product Test",
                                Money.of(new BigDecimal("25.00")),
                                Quantity.of(1))),
                Money.of(new BigDecimal("25.00")),
                OrderStatus.READY_FOR_DELIVERY,
                address,
                shippingCost,
                Money.of(new BigDecimal("35.00")),
                Instant.now(),
                Instant.now());
        when(orderRepository.findAll()).thenReturn(List.of(order));
        List<ReadyOrder> result = useCase.execute(null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).orderId()).isEqualTo(orderId);
        assertThat(result.get(0).deliveryAddress()).isEqualTo(address.toString());
        assertThat(result.get(0).shippingCost())
                .isEqualByComparingTo(new BigDecimal("10.00"));
    }
    @Test
    @DisplayName("Should return only orders with READY_FOR_DELIVERY status")
    void shouldReturnOnlyOrdersReadyForDelivery() {
        Order readyOrder = createOrder(OrderStatus.READY_FOR_DELIVERY);
        Order createdOrder = createOrder(OrderStatus.CREATED);
        when(orderRepository.findAll()).thenReturn(List.of(readyOrder, createdOrder));
        List<ReadyOrder> result = useCase.execute(null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).orderId()).isEqualTo(readyOrder.getId().value());
    }
    private Order createOrder(OrderStatus status) {
        UUID orderId = UUID.randomUUID();
        Order order = Order.restore(
                OrderId.of(orderId),
                CustomerId.of(UUID.randomUUID()),
                List.of(OrderLine.of(
                                ProductId.of(UUID.randomUUID()),
                                UUID.randomUUID(),
                                "Product Test",
                                Money.of(new BigDecimal("25.00")),
                                Quantity.of(1))),
                Money.of(new BigDecimal("25.00")),
                status,
                new DeliveryAddress("Cochabamba", "Av. América", "123"),
                Money.of(new BigDecimal("10.00")),
                Money.of(new BigDecimal("35.00")),
                Instant.now(),
                Instant.now());
        return order;
    }
    @Test
    @DisplayName("Should return an empty list when there are no ready orders")
    void shouldReturnEmptyListWhenThereAreNoReadyOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());
        List<ReadyOrder> result = useCase.execute(null);
        assertThat(result).isEmpty();
    }
}
