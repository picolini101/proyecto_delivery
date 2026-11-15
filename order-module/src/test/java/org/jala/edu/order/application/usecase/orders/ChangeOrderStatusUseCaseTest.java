package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;
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

class ChangeOrderStatusUseCaseTest {

    private InMemoryOrderRepositoryAdapter orderRepository;
    private InMemoryOrderStatusHistoryAdapter statusHistory;
    private ChangeOrderStatusUseCase useCase;

    private final UUID customerId = UUID.randomUUID();
    private final UUID adminUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        useCase = new ChangeOrderStatusUseCase(orderRepository, statusHistory, TransactionRunner.direct());
    }

    private Order createSampleOrder() {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Producto Test",
                Money.of(new BigDecimal("25.00")),
                Quantity.of(1)
        );
        Order order = Order.create(CustomerId.of(customerId), List.of(line), line.getSubtotal());
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("Criterio 1: Transición permitida actualiza estado y registra historial atómicamente")
    void shouldUpdateStatusAndRecordHistorySuccessfully() {
        Order order = createSampleOrder(); // Estado CREATED

        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
                order.getId().value(),
                OrderStatus.CONFIRMED,
                adminUserId,
                null
        );

        useCase.execute(command);

        // Verificamos que el pedido cambió a CONFIRMED
        Order updated = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // Verificamos que se guardó el registro de auditoría
        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertThat(history).hasSize(1);
        OrderStatusChange change = history.get(0);
        assertThat(change.getPreviousStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(change.getNewStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(change.getChangedById()).isEqualTo(adminUserId);
        assertThat(change.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Criterio 2: Transición no permitida lanza InvalidStateTransitionException y no guarda historial")
    void shouldRejectInvalidTransitionAndNotRecordHistory() {
        Order order = createSampleOrder(); // Estado CREATED

        // Intentar saltar de CREATED a DELIVERED directamente (no permitido)
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
                order.getId().value(),
                OrderStatus.DELIVERED,
                adminUserId,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidStateTransitionException.class);

        // El estado debe seguir siendo CREATED
        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.CREATED);

        // No debe haber historial registrado
        assertThat(statusHistory.findByOrderId(order.getId())).isEmpty();
    }

    @Test
    @DisplayName("Criterio 3: Pedido en estado final (DELIVERED o CANCELLED) rechaza cualquier transición")
    void shouldRejectAnyTransitionFromFinalState() {
        Order order = createSampleOrder();
        // Pasamos a CONFIRMED y luego a CANCELLED (estado final)
        order.transitionTo(OrderStatus.CANCELLED, "Cliente solicitó cancelación");
        orderRepository.save(order);

        // Intentar cambiar un pedido CANCELLED a CONFIRMED
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(
                order.getId().value(),
                OrderStatus.CONFIRMED,
                adminUserId,
                null
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("Criterio 4: Transición a CANCELLED exige motivo obligatorio")
    void shouldRequireReasonWhenCancellingOrder() {
        Order order = createSampleOrder(); // CREATED

        // Intentar cancelar sin motivo
        ChangeOrderStatusCommand commandWithoutReason = new ChangeOrderStatusCommand(
                order.getId().value(),
                OrderStatus.CANCELLED,
                adminUserId,
                "   "
        );

        assertThatThrownBy(() -> useCase.execute(commandWithoutReason))
                .isInstanceOf(DomainException.class);

        // Con motivo válido sí funciona
        ChangeOrderStatusCommand commandWithReason = new ChangeOrderStatusCommand(
                order.getId().value(),
                OrderStatus.CANCELLED,
                adminUserId,
                "Pago rechazado por el banco"
        );

        useCase.execute(commandWithReason);

        Order cancelledOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
