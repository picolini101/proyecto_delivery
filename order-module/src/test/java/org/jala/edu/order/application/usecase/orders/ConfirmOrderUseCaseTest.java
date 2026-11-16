package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.ConfirmOrderCommand;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfirmOrderUseCaseTest {

    private InMemoryOrderRepositoryAdapter orderRepository;
    private InMemoryOrderStatusHistoryAdapter statusHistory;
    private ConfirmOrderUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        useCase = new ConfirmOrderUseCase(orderRepository, statusHistory, TransactionRunner.direct());
    }

    private Order createSampleOrder() {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Pizza Suprema",
                Money.of(new BigDecimal("35.00")),
                Quantity.of(2)
        );
        Order order = Order.create(CustomerId.of(customerId), List.of(line), line.getSubtotal());
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("CA-1: Al confirmarse con pago aprobado, actualiza a CONFIRMED y registra auditoria")
    void shouldConfirmOrderSuccessfullyWhenPaymentIsApproved() {
        // Arrange
        Order order = createSampleOrder();

        PaymentAttempt approvedPayment = new PaymentAttempt(
                order.getId().value(),
                order.getSubtotal().value(),
                new PaymentAuthorization.Approved("PAY-REF-999")
        );

        ConfirmOrderCommand command = new ConfirmOrderCommand(order.getId().value(), customerId, approvedPayment);

        // Act
        OrderSummary summary = useCase.execute(command);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.status()).isEqualTo("CONFIRMED");
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getPreviousStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(history.get(0).getNewStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CA-2: Rechazar confirmacion si el pago no fue aprobado")
    void shouldRejectConfirmationWhenPaymentIsNotApproved() {
        // Arrange
        Order order = createSampleOrder();
        PaymentAttempt rejectedPayment = new PaymentAttempt(
                order.getId().value(),
                order.getSubtotal().value(),
                new PaymentAuthorization.Rejected("PAY-REF-000", "Fondos insuficientes")
        );

        ConfirmOrderCommand command = new ConfirmOrderCommand(order.getId().value(), customerId, rejectedPayment);

        // Act & Assert
        try {
            useCase.execute(command);
            fail("Debio lanzar DomainException al estar el pago rechazado");
        } catch (DomainException ex) {
            assertThat(ex.getMessage()).contains("pago no fue aprobado");
        }

        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(statusHistory.findByOrderId(order.getId())).isEmpty();
    }

    @Test
    @DisplayName("CA-3: La maquina de estados impide confirmar si el pedido ya esta cancelado")
    void shouldRejectConfirmationWhenOrderIsAlreadyCancelled() {
        // Arrange
        Order order = createSampleOrder();
        order.transitionTo(OrderStatus.CANCELLED, "Cancelado por el cliente");
        orderRepository.save(order);

        PaymentAttempt approvedPayment = new PaymentAttempt(
                order.getId().value(),
                order.getSubtotal().value(),
                new PaymentAuthorization.Approved("PAY-REF-111")
        );

        ConfirmOrderCommand command = new ConfirmOrderCommand(order.getId().value(), customerId, approvedPayment);

        // Act & Assert
        try {
            useCase.execute(command);
            fail("Debio lanzar InvalidStateTransitionException porque la maquina de estados es intocable");
        } catch (InvalidStateTransitionException ex) {
            assertThat(ex.getMessage()).isNotNull();
        }
    }

}
