package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.application.usecase.orders.ChangeOrderStatusUseCase;
import org.jala.edu.order.application.usecase.orders.RegisterFailedPaymentUseCase;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.PaymentAttemptResult;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/** Prueba de punta a punta: un pago rechazado cancela el pedido y queda guardado en la base de datos. */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:register-failed-payment;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.flyway.locations=classpath:db/migration/order"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({OrderPersistenceAdapter.class, OrderPersistenceMapper.class, OrderLinePersistenceMapper.class,
        OrderStatusHistoryPersistenceAdapter.class, OrderStatusHistoryPersistenceMapper.class,
        FailedPaymentLogPersistenceAdapter.class, FailedPaymentPersistenceMapper.class})
class RegisterFailedPaymentJpaTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private OrderPersistenceAdapter orders;
    @Autowired
    private OrderStatusHistoryPersistenceAdapter history;
    @Autowired
    private FailedPaymentLogPersistenceAdapter failedPayments;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCancelTheOrderAndSaveTheFailedPaymentInTheDatabaseWhenThePaymentIsRejected() {
        // Arrange
        Order order = orders.save(createOrder());
        RegisterFailedPaymentUseCase useCase = new RegisterFailedPaymentUseCase(
                new ChangeOrderStatusUseCase(orders, history, TransactionRunner.direct()), failedPayments);
        PaymentAttempt attempt = new PaymentAttempt(order.getId().value(), new BigDecimal("6000.00"),
                new PaymentAuthorization.Rejected("REF-1", "El monto excede el limite permitido"));

        // Act
        useCase.execute(new RegisterFailedPaymentCommand(attempt, CUSTOMER_ID));
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(orders.findById(order.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(failedPayments.findByOrderId(order.getId()))
                .singleElement()
                .satisfies(record -> {
                    assertThat(record.getCustomerId()).isEqualTo(CUSTOMER_ID);
                    assertThat(record.getResult()).isEqualTo(PaymentAttemptResult.REJECTED);
                    assertThat(record.getReason()).isEqualTo("El monto excede el limite permitido");
                });
    }

    private static Order createOrder() {
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), UUID.randomUUID(), "Tea",
                Money.of(new BigDecimal("5.00")), Quantity.of(1));
        return Order.create(CustomerId.of(UUID.randomUUID()), List.of(line), Money.of(5.00));
    }
}