package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.in.CancelOrderCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CancelOrderUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private OrderStatusHistoryPort statusHistory;
    private CancelOrderUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID otherCustomerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        TransactionRunner transactions = TransactionRunner.direct();
        useCase = new CancelOrderUseCase(orderRepository, statusHistory, transactions);
    }

    private Order createTestOrder(OrderStatus initialStatus, UUID ownerId) {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Pizza Test",
                Money.of(new BigDecimal("20.00")),
                Quantity.of(1)
        );
        Order order = Order.create(CustomerId.of(ownerId), List.of(line), line.getSubtotal());

        if (initialStatus != OrderStatus.CREATED) {
            order.transitionTo(initialStatus, "Avance inicial de prueba");
        }
        return orderRepository.save(order);
    }

    @Test
    void shouldCancelOrderSuccessfullyWhenStatusIsCreated() {
        Order order = createTestOrder(OrderStatus.CREATED, customerId);
        CancelOrderCommand command = new CancelOrderCommand(order.getId().value(), customerId, "Me equivoqué de productos");

        useCase.execute(command);

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());

        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertEquals(1, history.size());
        OrderStatusChange change = history.get(0);
        assertEquals(OrderStatus.CREATED, change.getPreviousStatus());
        assertEquals(OrderStatus.CANCELLED, change.getNewStatus());
        assertEquals("Me equivoqué de productos", change.getReason());
        assertEquals(customerId, change.getChangedById());
    }

    @Test
    void shouldCancelOrderSuccessfullyWhenStatusIsConfirmed() {
        Order order = createTestOrder(OrderStatus.CONFIRMED, customerId);
        CancelOrderCommand command = new CancelOrderCommand(order.getId().value(), customerId, "Ya no deseo el pedido");

        useCase.execute(command);

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());

        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertTrue(history.stream().anyMatch(change ->
                change.getPreviousStatus() == OrderStatus.CONFIRMED
                        && change.getNewStatus() == OrderStatus.CANCELLED
                        && "Ya no deseo el pedido".equals(change.getReason())
        ));
    }

    @Test
    void shouldThrowDomainExceptionWhenReasonIsNullOrEmptyOrSpaces() {
        Order order = createTestOrder(OrderStatus.CREATED, customerId);

        CancelOrderCommand nullReasonCommand = new CancelOrderCommand(order.getId().value(), customerId, null);
        assertThrows(DomainException.class, () -> useCase.execute(nullReasonCommand));

        CancelOrderCommand emptyReasonCommand = new CancelOrderCommand(order.getId().value(), customerId, "");
        assertThrows(DomainException.class, () -> useCase.execute(emptyReasonCommand));

        CancelOrderCommand whitespaceReasonCommand = new CancelOrderCommand(order.getId().value(), customerId, "   ");
        assertThrows(DomainException.class, () -> useCase.execute(whitespaceReasonCommand));

        // Verificar que el estado no cambió
        Order notChangedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CREATED, notChangedOrder.getStatus());
    }

    @Test
    void shouldThrowUnauthorizedCustomerExceptionWhenOrderBelongsToAnotherCustomer() {
        Order order = createTestOrder(OrderStatus.CREATED, otherCustomerId);
        CancelOrderCommand command = new CancelOrderCommand(order.getId().value(), customerId, "Motivo válido");

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenOrderDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        CancelOrderCommand command = new CancelOrderCommand(nonExistentId, customerId, "Motivo válido");

        assertThrows(NotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldThrowInvalidStateTransitionExceptionWhenOrderIsInNonCancellableStatus() {
        Order order = createTestOrder(OrderStatus.CONFIRMED, customerId);
        order.transitionTo(OrderStatus.READY_FOR_DELIVERY, "En cocina");
        orderRepository.save(order);

        CancelOrderCommand command = new CancelOrderCommand(order.getId().value(), customerId, "Quiero cancelar tarde");

        assertThrows(InvalidStateTransitionException.class, () -> useCase.execute(command));
    }
}