package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.AuthorizePaymentCommand;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.application.port.out.PaymentGatewayPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link AuthorizePaymentUseCase} (HU-10).
 * Usa los usuarios de prueba del core y el repositorio de pedidos en memoria; el puerto de pagos es un mock
 * (objeto falso) para poder simular aprobado, rechazado y error del servicio.
 * El pedido de prueba tiene 2 pizzas a 50.00: subtotal 100.00 y, con el envio de 10.00, total 110.00.
 */
class AuthorizePaymentUseCaseTest {

    /** Metodo de pago de prueba del cliente de ejemplo. */
    private static final UUID METHOD = SamplePaymentMethods.CUSTOMER_CARD;

    /** Repositorio de pedidos en memoria. */
    private InMemoryOrderRepositoryAdapter orders;

    /** Puerto de pagos falso. */
    private PaymentGatewayPort gateway;

    /** Caso de uso que se esta probando. */
    private AuthorizePaymentUseCase useCase;

    /** Pedido de prueba, guardado en estado CREATED. */
    private Order order;

    /** Se ejecuta antes de cada prueba: arma el caso de uso y guarda un pedido pendiente de pago. */
    @BeforeEach
    void setUp() {
        orders = new InMemoryOrderRepositoryAdapter();
        gateway = mock(PaymentGatewayPort.class);
        useCase = new AuthorizePaymentUseCase(new FallbackUserService(), orders, gateway);

        order = orders.save(Order.create(CustomerId.of(SampleUsers.CUSTOMER), lines(), Money.of(100)));
    }

    /** Una linea de 2 pizzas a 50.00. */
    private List<OrderLine> lines() {
        return List.of(OrderLine.of(ProductId.of(SampleCatalog.PIZZA), "Pizza", Money.of(50), Quantity.of(2)));
    }

    /** Arma el comando para pagar el pedido de prueba como el cliente de ejemplo. */
    private AuthorizePaymentCommand command() {
        return new AuthorizePaymentCommand(order.getId().value(), SampleUsers.CUSTOMER, METHOD);
    }

    /** Criterio 1: se envia el monto TOTAL (productos mas envio) y, si se aprueba, el flujo puede continuar. */
    @Test
    void approvedPaymentSendsTheTotalAmount() {
        when(gateway.authorize(any(), any(), any(), any())).thenReturn(new PaymentAuthorization.Approved("REF-1"));

        PaymentAttempt attempt = useCase.execute(command());

        assertTrue(attempt.isApproved());
        assertEquals(new BigDecimal("110.00"), attempt.amount());
        verify(gateway).authorize(order.getId().value(), SampleUsers.CUSTOMER, METHOD, new BigDecimal("110.00"));
    }

    /** Criterio 2 (A4): el pago se rechaza, se informa el motivo y el pedido no cambia. */
    @Test
    void rejectedPaymentReportsReasonAndDoesNotChangeTheOrder() {
        when(gateway.authorize(any(), any(), any(), any()))
                .thenReturn(new PaymentAuthorization.Rejected("REF-2", "Fondos insuficientes"));

        PaymentAttempt attempt = useCase.execute(command());

        assertFalse(attempt.isApproved());
        assertInstanceOf(PaymentAuthorization.Rejected.class, attempt.authorization());
        assertEquals("Fondos insuficientes", attempt.message());
        assertEquals(OrderStatus.CREATED, orders.findById(order.getId()).orElseThrow().getStatus());
    }

    /** Criterio 3: el servicio no responde, se muestra el motivo y el pedido no cambia. */
    @Test
    void unavailableServiceReportsReasonAndDoesNotChangeTheOrder() {
        when(gateway.authorize(any(), any(), any(), any()))
                .thenReturn(new PaymentAuthorization.Unavailable("El servicio de pagos no esta disponible"));

        PaymentAttempt attempt = useCase.execute(command());

        assertFalse(attempt.isApproved());
        assertInstanceOf(PaymentAuthorization.Unavailable.class, attempt.authorization());
        assertEquals("El servicio de pagos no esta disponible", attempt.message());
        assertEquals(OrderStatus.CREATED, orders.findById(order.getId()).orElseThrow().getStatus());
    }

    /** Un cliente inactivo no puede pagar y no se llama al servicio de pagos. */
    @Test
    void inactiveCustomerIsNotAuthorized() {
        AuthorizePaymentCommand inactive =
                new AuthorizePaymentCommand(order.getId().value(), SampleUsers.INACTIVE_CUSTOMER, METHOD);

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(inactive));
        verify(gateway, never()).authorize(any(), any(), any(), any());
    }

    /** Un pedido que no existe no se cobra. */
    @Test
    void unknownOrderIsNotCharged() {
        AuthorizePaymentCommand unknown = new AuthorizePaymentCommand(UUID.randomUUID(), SampleUsers.CUSTOMER, METHOD);

        assertThrows(NotFoundException.class, () -> useCase.execute(unknown));
        verify(gateway, never()).authorize(any(), any(), any(), any());
    }

    /** Un pedido de otro cliente no se cobra. */
    @Test
    void orderOfAnotherCustomerIsNotCharged() {
        Order foreign = orders.save(Order.create(CustomerId.of(UUID.randomUUID()), lines(), Money.of(100)));
        AuthorizePaymentCommand command =
                new AuthorizePaymentCommand(foreign.getId().value(), SampleUsers.CUSTOMER, METHOD);

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(command));
        verify(gateway, never()).authorize(any(), any(), any(), any());
    }

    /** Un pedido que ya no esta en CREATED (por ejemplo confirmado) no se cobra otra vez. */
    @Test
    void orderThatIsNotPendingIsNotCharged() {
        Order confirmed = orders.save(Order.restore(order.getId(), order.getCustomerId(), lines(), Money.of(100),
                OrderStatus.CONFIRMED, Instant.now(), Instant.now()));
        AuthorizePaymentCommand command =
                new AuthorizePaymentCommand(confirmed.getId().value(), SampleUsers.CUSTOMER, METHOD);

        assertThrows(ConflictException.class, () -> useCase.execute(command));
        verify(gateway, never()).authorize(any(), any(), any(), any());
    }
}
