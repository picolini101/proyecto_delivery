package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.order.application.usecase.orders.ChangeOrderStatusUseCase;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/** Contrato de persistencia JPA esperado para el historial de estados de pedidos. */

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-status-history;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.locations=classpath:db/migration/order"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderPersistenceAdapter.class, OrderPersistenceMapper.class, OrderLinePersistenceMapper.class,
        OrderStatusHistoryPersistenceAdapter.class, OrderStatusHistoryPersistenceMapper.class})
class OrderStatusHistoryPersistenceAdapterTest {
    @Autowired
    private OrderPersistenceAdapter orders;
    @Autowired
    private OrderStatusHistoryPersistenceAdapter history;
    @Autowired
    private EntityManager entityManager;
    private static final UUID CHANGED_BY = UUID.fromString("33333333-3333-3333-3333-333333333333");
    @Test
    void shouldFindStatusChangeById() {
        // Arrange
        Order order = orders.save(createOrder());
        OrderStatusChange change = history.save(
                OrderStatusChange.record(
                        order.getId(),
                        OrderStatus.CREATED,
                        OrderStatus.CONFIRMED,
                        CHANGED_BY,
                        "Payment authorized"));
        entityManager.flush();
        entityManager.clear();
        // Act
        OrderStatusChange found = history.findById(change.getId()) .orElseThrow();;
        // Assert
        assertThat(found.getId()).isEqualTo(change.getId());
        assertThat(found.getNewStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
    @Test
    void shouldFindChangesByOrderId() {
        // Arrange
        Order order = orders.save(createOrder());
        history.save(OrderStatusChange.record(
                        order.getId(),
                        OrderStatus.CREATED,
                        OrderStatus.CONFIRMED,
                        CHANGED_BY,
                        "Confirmed"));
        entityManager.flush();
        entityManager.clear();
        // Act
        List<OrderStatusChange> result = history.findByOrderId(order.getId());
        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void confirmsPersistedOrderAndRecordsItsStatusTransition() {
        // Arrange
        Order order = orders.save(createOrder());
        ChangeOrderStatusUseCase useCase = new ChangeOrderStatusUseCase(
                orders, history, TransactionRunner.direct());
        ChangeOrderStatusCommand command = new ChangeOrderStatusCommand(order.getId().value(),
                OrderStatus.CONFIRMED, CHANGED_BY, "Payment authorized");

        // Act
        useCase.execute(command);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Order savedOrder = orders.findById(order.getId()).orElseThrow();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(history.findByOrderId(order.getId()))
                .singleElement()
                .satisfies(change -> {
                    assertThat(change.getPreviousStatus()).isEqualTo(OrderStatus.CREATED);
                    assertThat(change.getNewStatus()).isEqualTo(OrderStatus.CONFIRMED);
                    assertThat(change.getChangedById()).isEqualTo(CHANGED_BY);
                });
    }
    private static Order createOrder() {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Tea",
                Money.of(new BigDecimal("5.00")),
                Quantity.of(1));
        return Order.create(
                CustomerId.of(UUID.randomUUID()), List.of(line), Money.of(5.00));
    }
}
