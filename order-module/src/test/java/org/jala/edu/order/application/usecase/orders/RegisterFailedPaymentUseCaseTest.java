package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;
import org.jala.edu.order.domain.model.PaymentAttemptResult;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryFailedPaymentLogAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderStatusHistoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link RegisterFailedPaymentUseCase} (HU "Registrar el intento de pago fallido").
 * Usa los repositorios en memoria; no hace falta ningun mock porque el pago ya viene resuelto en el intento.
 * Criterio 1: si el pago es rechazado, el pedido pasa a Cancelado con el motivo "Pago rechazado" en el historial.
 * Criterio 2: el intento fallido queda registrado (resultado, fecha y hora) para auditoria.
 */
class RegisterFailedPaymentUseCaseTest {

    /** Repositorio de pedidos en memoria. */
    private InMemoryOrderRepositoryAdapter orders;

    /** Historial de cambios de estado en memoria. */
    private InMemoryOrderStatusHistoryAdapter statusHistory;

    /** Registro de intentos de pago fallidos en memoria (auditoria). */
    private InMemoryFailedPaymentLogAdapter failedPayments;

    /** Caso de uso que se esta probando. */
    private RegisterFailedPaymentUseCase useCase;

    /** Pedido de prueba, guardado en estado CREATED (pendiente de pago). */
    private Order order;

    /** Se ejecuta antes de cada prueba: arma el caso de uso y guarda un pedido pendiente de pago. */
    @BeforeEach
    void setUp() {
        orders = new InMemoryOrderRepositoryAdapter();
        statusHistory = new InMemoryOrderStatusHistoryAdapter();
        failedPayments = new InMemoryFailedPaymentLogAdapter();
        ChangeOrderStatusUseCase changeOrderStatus =
                new ChangeOrderStatusUseCase(orders, statusHistory, TransactionRunner.direct());
        useCase = new RegisterFailedPaymentUseCase(changeOrderStatus, failedPayments);

        order = orders.save(Order.create(CustomerId.of(SampleUsers.CUSTOMER), lines(), Money.of(100)));
    }

    /** Una linea de 2 pizzas a 50.00. */
    private List<OrderLine> lines() {
        return List.of(OrderLine.of(ProductId.of(SampleCatalog.PIZZA), "Pizza", Money.of(50), Quantity.of(2)));
    }

    /** Arma el comando con un pago RECHAZADO del pedido de prueba. */
    private RegisterFailedPaymentCommand rejectedCommand() {
        PaymentAttempt attempt = new PaymentAttempt(order.getId().value(), new BigDecimal("110.00"),
                new PaymentAuthorization.Rejected("REF-2", "Fondos insuficientes"));
        return new RegisterFailedPaymentCommand(attempt, SampleUsers.CUSTOMER);
    }

    /** Criterio 1: el pago rechazado deja el pedido en CANCELLED. */
    @Test
    void rejectedPaymentCancelsTheOrder() {
        useCase.execute(rejectedCommand());

        Order saved = orders.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.CANCELLED, saved.getStatus());
    }

    /** Criterio 1: el historial guarda el motivo automatico "Pago rechazado". */
    @Test
    void rejectedPaymentSavesTheAutomaticReasonInTheHistory() {
        useCase.execute(rejectedCommand());

        List<OrderStatusChange> history = statusHistory.findByOrderId(order.getId());
        assertEquals(1, history.size());
        assertEquals(OrderStatus.CREATED, history.get(0).getPreviousStatus());
        assertEquals(OrderStatus.CANCELLED, history.get(0).getNewStatus());
        assertEquals("Pago rechazado", history.get(0).getReason());
    }

    /** Criterio 1: el pedido nunca queda confirmado (confirmado = pagado). */
    @Test
    void rejectedPaymentNeverLeavesTheOrderConfirmed() {
        useCase.execute(rejectedCommand());

        Order saved = orders.findById(order.getId()).orElseThrow();
        assertNotEquals(OrderStatus.CONFIRMED, saved.getStatus());
    }

    /** Criterio 2: se guarda un registro con el resultado y el motivo del rechazo. */
    @Test
    void rejectedPaymentRegistersTheFailedAttempt() {
        useCase.execute(rejectedCommand());

        List<FailedPaymentRecord> records = failedPayments.findByOrderId(order.getId());
        assertEquals(1, records.size());
        FailedPaymentRecord record = records.get(0);
        assertEquals(order.getId(), record.getOrderId());
        assertEquals(SampleUsers.CUSTOMER, record.getCustomerId());
        assertEquals(PaymentAttemptResult.REJECTED, record.getResult());
        assertEquals("Fondos insuficientes", record.getReason());
    }

    /** Criterio 2: el registro guarda la fecha y la hora en que ocurrio el intento. */
    @Test
    void failedAttemptKeepsTheDateAndTime() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.MICROS);

        useCase.execute(rejectedCommand());

        Instant after = Instant.now();
        FailedPaymentRecord record = failedPayments.findByOrderId(order.getId()).get(0);
        assertFalse(record.getCreatedAt().isBefore(before));
        assertFalse(record.getCreatedAt().isAfter(after));
    }
}