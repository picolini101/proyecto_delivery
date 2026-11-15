package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/** Contrato de persistencia JPA esperado para pedidos. */

@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:orders;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderPersistenceAdapter.class)
class OrderPersistenceAdapterTest {

    @Autowired
    private OrderPersistenceAdapter adapter;

    @Autowired
    private EntityManager entityManager;

    private static final UUID CUSTOMER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void shouldFindOrderById() {

        // Arrange
        Order saved = adapter.save(createOrder(CUSTOMER_ID));

        entityManager.flush();
        entityManager.clear();

        // Act
        Order found = adapter.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getLines()).hasSize(1);
    }

    @Test
    void shouldUpdateOrder() {

        // Arrange
        Order order = adapter.save(createOrder(CUSTOMER_ID));
        entityManager.flush();
        entityManager.clear();

        order.transitionTo(OrderStatus.CONFIRMED, null);

        // Act
        adapter.save(order);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Order found = adapter.findById(order.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldFindAllOrders() {

        // Arrange
        adapter.save(createOrder(CUSTOMER_ID));
        adapter.save(createOrder(CUSTOMER_ID));

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Order> found = adapter.findAll();

        // Assert
        assertThat(found).hasSize(2);
    }

    @Test
    void shouldFindOrdersByCustomerId() {

        // Arrange
        adapter.save(createOrder(CUSTOMER_ID));

        UUID otherCustomer = UUID.fromString(
                "22222222-2222-2222-2222-222222222222"
        );

        adapter.save(createOrder(otherCustomer));

        entityManager.flush();
        entityManager.clear();

        // Act
        List<Order> found =
                adapter.findByCustomerId(CustomerId.of(CUSTOMER_ID));

        // Assert
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getCustomerId()).isEqualTo(CUSTOMER_ID);
    }

    private static Order createOrder(UUID customerId) {

        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Coffee",
                Money.of(new BigDecimal("12.50")),
                Quantity.of(1)
        );

        return Order.restore(
                OrderId.newId(),
                CustomerId.of(customerId),
                List.of(line),
                Money.of(12.50),
                OrderStatus.CREATED,
                Instant.now(),
                Instant.now()
        );
    }
}
