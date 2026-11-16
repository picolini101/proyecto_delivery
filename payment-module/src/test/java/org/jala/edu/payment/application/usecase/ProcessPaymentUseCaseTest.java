
package org.jala.edu.payment.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessPaymentUseCaseTest {

    private UserService userService;
    private PaymentMethodValidationPort methodPort;
    private PaymentTransactionPort transactionPort;
    private ProcessPaymentUseCase useCase;

    private UUID orderId;
    private UUID userId;
    private UUID paymentMethodId;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        methodPort = mock(PaymentMethodValidationPort.class);
        transactionPort = mock(PaymentTransactionPort.class);

        useCase = new ProcessPaymentUseCase(
                userService, methodPort, transactionPort);

        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
    }

    @Test
    void shouldRejectNullRequest() {
        PaymentResult result = useCase.execute(null);

        assertFalse(result.approved());
        verifyNoInteractions(transactionPort);
    }

    @Test
    void shouldRejectMissingOrderId() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        null, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());

        verify(transactionPort).recordRejected(
                isNull(),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class),
                eq("El pedido es obligatorio"));
    }

    @Test
    void shouldRejectZeroAmount() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        BigDecimal.ZERO);

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());

        verify(transactionPort).recordRejected(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                eq(BigDecimal.ZERO),
                eq("El monto debe ser mayor a cero"));
    }

    @Test
    void shouldRejectAmountAboveLimit() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("5000.01"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals(
                "El monto excede el limite permitido",
                result.reason());
    }

    @Test
    void shouldRejectUnauthorizedUser() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(false);

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals(
                "User not authorized for this operation",
                result.reason());

        verifyNoInteractions(transactionPort);
    }

    @Test
    void shouldRejectMissingPaymentMethod() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, null,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals(
                "El metodo de pago es obligatorio",
                result.reason());

        verifyNoInteractions(methodPort);
    }

    @Test
    void shouldRejectInvalidPaymentMethod() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        when(methodPort.isValidPaymentMethod(
                userId, paymentMethodId)).thenReturn(false);

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals(
                "Metodo de pago invalido o inactivo",
                result.reason());

        verify(transactionPort, never()).recordApproved(
                any(), any(), any(), any());
    }

    @Test
    void shouldApproveValidPayment() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        when(methodPort.isValidPaymentMethod(
                userId, paymentMethodId)).thenReturn(true);

        when(transactionPort.recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class)))
                .thenReturn("PAY-001");

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertTrue(result.approved());
        assertEquals("PAY-001", result.paymentReference());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class));

        verify(transactionPort, never()).recordRejected(
                any(), any(), any(), any(), anyString());
    }


    @Test
    void shouldRejectDuplicatePayment() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        when(methodPort.isValidPaymentMethod(
                userId, paymentMethodId)).thenReturn(true);

        when(transactionPort.recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class)))
                .thenThrow(new IllegalStateException(
                        "Intento de cobro duplicado sobre pedido aprobado"));

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals("", result.paymentReference());
        assertEquals(
                "Intento de cobro duplicado sobre pedido aprobado",
                result.reason());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class));

        verify(transactionPort, never()).recordRejected(
                any(), any(), any(), any(), anyString());
    }
    @Test
    void shouldRejectPaymentWhenTransactionRecordingFailsUnexpectedly() {
        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        when(methodPort.isValidPaymentMethod(
                userId, paymentMethodId)).thenReturn(true);

        when(transactionPort.recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Error inesperado"));

        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100"));

        PaymentResult result = useCase.execute(command);

        assertFalse(result.approved());
        assertEquals("", result.paymentReference());
        assertEquals(
                "No se pudo procesar el pago",
                result.reason());
    }

}