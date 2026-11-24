package org.jala.edu.payment.application.usecase;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;
import org.jala.edu.payment.application.port.out.PaymentTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    // Solicitud nula

    @Test
    void shouldRejectNullRequest() {
        PaymentResult result = useCase.execute(null);

        assertFalse(result.approved());
        verifyNoInteractions(transactionPort);
    }

    // Pedido obligatorio

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

    // HU-32: Monto cero

    @Test
    void shouldRejectZeroAmount() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId,
                        userId,
                        paymentMethodId,
                        BigDecimal.ZERO
                );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.execute(command)
        );

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage()
        );

        verifyNoInteractions(transactionPort);
    }

    // HU-32: Monto negativo

    @Test
    void shouldRejectNegativeAmountWithoutRecording() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId,
                        userId,
                        paymentMethodId,
                        new BigDecimal("-15.00")
                );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.execute(command)
        );

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage()
        );

        verifyNoInteractions(transactionPort);
    }

    // HU-32: Valor negativo cercano a cero

    @Test
    void shouldRejectNegativeMinimumAmount() {
        ProcessPaymentCommand command =
                new ProcessPaymentCommand(
                        orderId,
                        userId,
                        paymentMethodId,
                        new BigDecimal("-0.01")
                );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.execute(command)
        );

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage()
        );

        verifyNoInteractions(transactionPort);
    }

    // HU-32: Monto mayor al limite

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

        verify(transactionPort, never()).recordApproved(
                any(), any(), any(), any());
    }

    // HU-32: Monto minimo permitido

    @Test
    void shouldAcceptMinimumAmount() {
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
                        orderId,
                        userId,
                        paymentMethodId,
                        new BigDecimal("0.01")
                );

        PaymentResult result = useCase.execute(command);

        assertTrue(result.approved());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                eq(new BigDecimal("0.01"))
        );
    }

    // HU-32: Monto maximo permitido

    @Test
    void shouldAcceptMaximumAmount() {
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
                        orderId,
                        userId,
                        paymentMethodId,
                        new BigDecimal("5000.00")
                );

        PaymentResult result = useCase.execute(command);

        assertTrue(result.approved());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                eq(new BigDecimal("5000.00"))
        );
    }

    // HU-32: Redondeo a dos decimales

    @Test
    void shouldRoundAmountBeforeProcessingPayment() {
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
                        orderId,
                        userId,
                        paymentMethodId,
                        new BigDecimal("100.505")
                );

        PaymentResult result = useCase.execute(command);

        assertTrue(result.approved());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                eq(new BigDecimal("100.51"))
        );
    }

    // Usuario no autorizado

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

    // Metodo de pago obligatorio

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

    // Metodo de pago invalido

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

    // Pago valido

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

        assertEquals(
                "PAY-001",
                result.paymentReference());

        verify(transactionPort).recordApproved(
                eq(orderId),
                eq(userId),
                eq(paymentMethodId),
                any(BigDecimal.class));

        verify(transactionPort, never()).recordRejected(
                any(), any(), any(), any(), anyString());
    }

    // Prevencion de cobro duplicado

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

        assertEquals(
                "",
                result.paymentReference());

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

    // Manejo de errores inesperados

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

        assertEquals(
                "",
                result.paymentReference());

        assertEquals(
                "No se pudo procesar el pago",
                result.reason());
    }
}
