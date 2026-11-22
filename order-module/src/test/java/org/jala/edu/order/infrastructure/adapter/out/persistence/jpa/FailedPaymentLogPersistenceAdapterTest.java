package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.FailedPaymentRecordId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.PaymentAttemptResult;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/** Contrato de persistencia JPA esperado para el registro de pagos fallidos. */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:failed-payment-log;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.locations=classpath:db/migration/order"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderPersistenceAdapter.class, OrderPersistenceMapper.class, OrderLinePersistenceMapper.class,
        FailedPaymentLogPersistenceAdapter.class, FailedPaymentPersistenceMapper.class})
class FailedPaymentLogPersistenceAdapterTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private OrderPersistenceAdapter orders;
    @Autowired
    private FailedPaymentLogPersistenceAdapter failedPayments;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindFailedPaymentById() {
        // Arrange
        Order order = orders.save(createOrder());
        FailedPaymentRecord saved = failedPayments.save(
                FailedPaymentRecord.record(order.getId(), CUSTOMER_ID, PaymentAttemptResult.REJECTED, "Pago rechazado"));
        entityManager.flush();
        entityManager.clear();

        // Act
        FailedPaymentRecord found = failedPayments.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getOrderId()).isEqualTo(order.getId());
        assertThat(found.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(found.getResult()).isEqualTo(PaymentAttemptResult.REJECTED);
        assertThat(found.getReason()).isEqualTo("Pago rechazado");
        assertThat(found.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void shouldFindFailedPaymentsByOrderIdOldestFirst() {
        // Arrange
        Order order = orders.save(createOrder());
        Instant older = Instant.parse("2026-11-21T10:00:00.000000Z");
        Instant newer = Instant.parse("2026-11-21T11:00:00.000000Z");
        failedPayments.save(restored(order.getId(), "Segundo intento", newer));
        failedPayments.save(restored(order.getId(), "Primer intento", older));
        entityManager.flush();
        entityManager.clear();

        // Act
        List<FailedPaymentRecord> result = failedPayments.findByOrderId(order.getId());

        // Assert
        assertThat(result).extracting(FailedPaymentRecord::getReason)
                .containsExactly("Primer intento", "Segundo intento");
    }

    @Test
    void shouldNotReturnFailedPaymentsOfAnotherOrder() {
        // Arrange
        Order order = orders.save(createOrder());
        Order otherOrder = orders.save(createOrder());
        failedPayments.save(
                FailedPaymentRecord.record(order.getId(), CUSTOMER_ID, PaymentAttemptResult.REJECTED, "Pago rechazado"));
        entityManager.flush();
        entityManager.clear();

        // Act
        List<FailedPaymentRecord> result = failedPayments.findByOrderId(otherOrder.getId());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectFailedPaymentOfUnknownOrder() {
        // Arrange
        FailedPaymentRecord orphan = FailedPaymentRecord.record(
                OrderId.newId(), CUSTOMER_ID, PaymentAttemptResult.REJECTED, "Pago rechazado");

        // Act + Assert
        assertThatThrownBy(() -> {
            failedPayments.save(orphan);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    private static FailedPaymentRecord restored(OrderId orderId, String reason, Instant createdAt) {
        return FailedPaymentRecord.restore(FailedPaymentRecordId.newId(), orderId, CUSTOMER_ID,
                PaymentAttemptResult.REJECTED, reason, createdAt, createdAt);
    }

    private static Order createOrder() {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Tea",
                Money.of(new BigDecimal("5.00")),
                Quantity.of(1));
        return Order.create(CustomerId.of(UUID.randomUUID()), List.of(line), Money.of(5.00));
    }
}