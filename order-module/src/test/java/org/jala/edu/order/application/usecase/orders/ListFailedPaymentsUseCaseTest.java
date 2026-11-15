package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.order.application.dto.FailedPaymentItem;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryFailedPaymentLogAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link ListFailedPaymentsUseCase}: los pedidos cancelados por pago rechazado que ve el cliente.
 * Los datos se preparan con el caso de uso real {@link RegisterFailedPaymentUseCase}.
 */
class ListFailedPaymentsUseCaseTest {

    private InMemoryOrderRepositoryAdapter orders;
    private RegisterFailedPaymentUseCase registerFailedPayment;
    private ListFailedPaymentsUseCase useCase;

    @BeforeEach
    void setUp() {
        orders = new InMemoryOrderRepositoryAdapter();
        InMemoryOrderStatusHistoryAdapter statusHistory = new InMemoryOrderStatusHistoryAdapter();
        InMemoryFailedPaymentLogAdapter failedPayments = new InMemoryFailedPaymentLogAdapter();
        ChangeOrderStatusUseCase changeOrderStatus =
                new ChangeOrderStatusUseCase(orders, statusHistory, TransactionRunner.direct());
        registerFailedPayment = new RegisterFailedPaymentUseCase(changeOrderStatus, failedPayments);
        useCase = new ListFailedPaymentsUseCase(failedPayments, statusHistory);
    }

    private Order newOrderOf(UUID customerId) {
        List<OrderLine> lines = List.of(
                OrderLine.of(ProductId.of(SampleCatalog.PIZZA), "Pizza", Money.of(50), Quantity.of(2)));
        return orders.save(Order.create(CustomerId.of(customerId), lines, Money.of(100)));
    }

    private void rejectPaymentOf(Order order, UUID customerId, String detail) {
        PaymentAttempt attempt = new PaymentAttempt(
                order.getId().value(), new BigDecimal("110.00"), new PaymentAuthorization.Rejected("REF", detail));
        registerFailedPayment.execute(new RegisterFailedPaymentCommand(attempt, customerId));
    }

    @Test
    void listsTheCancelledOrderWithReasonDetailAndDate() {
        Order order = newOrderOf(SampleUsers.CUSTOMER);
        rejectPaymentOf(order, SampleUsers.CUSTOMER, "Fondos insuficientes");

        List<FailedPaymentItem> items = useCase.execute(SampleUsers.CUSTOMER);

        assertEquals(1, items.size());
        assertEquals(order.getId().value(), items.get(0).orderId());
        assertEquals("Pago rechazado", items.get(0).cancellationReason());
        assertEquals("Fondos insuficientes", items.get(0).paymentDetail());
        assertNotNull(items.get(0).attemptedAt());
    }

    @Test
    void doesNotListFailedPaymentsOfOtherCustomers() {
        UUID other = UUID.randomUUID();
        rejectPaymentOf(newOrderOf(other), other, "Fondos insuficientes");

        assertTrue(useCase.execute(SampleUsers.CUSTOMER).isEmpty());
    }

    @Test
    void newestFailedPaymentComesFirst() throws InterruptedException {
        Order older = newOrderOf(SampleUsers.CUSTOMER);
        rejectPaymentOf(older, SampleUsers.CUSTOMER, "Primero");
        Thread.sleep(5);
        Order newer = newOrderOf(SampleUsers.CUSTOMER);
        rejectPaymentOf(newer, SampleUsers.CUSTOMER, "Segundo");

        List<FailedPaymentItem> items = useCase.execute(SampleUsers.CUSTOMER);

        assertEquals(newer.getId().value(), items.get(0).orderId());
        assertEquals(older.getId().value(), items.get(1).orderId());
    }
}
