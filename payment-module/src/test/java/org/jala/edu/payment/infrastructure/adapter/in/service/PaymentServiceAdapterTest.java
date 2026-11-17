package org.jala.edu.payment.infrastructure.adapter.in.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.payment.application.port.in.ProcessPaymentCommand;
import org.jala.edu.payment.application.usecase.ProcessPaymentUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PaymentServiceAdapterTest {

    @Test
    void shouldDelegatePaymentToUseCase() {
        ProcessPaymentUseCase useCase =
                mock(ProcessPaymentUseCase.class);

        PaymentServiceAdapter adapter =
                new PaymentServiceAdapter(useCase);

        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID methodId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100");

        PaymentResult expected =
                new PaymentResult("PAY-001", true, "");

        when(useCase.execute(any(ProcessPaymentCommand.class)))
                .thenReturn(expected);

        PaymentResult result =
                adapter.pay(orderId, userId, methodId, amount);

        assertTrue(result.approved());
        assertEquals("PAY-001", result.paymentReference());

        ArgumentCaptor<ProcessPaymentCommand> captor =
                ArgumentCaptor.forClass(ProcessPaymentCommand.class);

        verify(useCase).execute(captor.capture());

        ProcessPaymentCommand command = captor.getValue();

        assertEquals(orderId, command.orderId());
        assertEquals(userId, command.userId());
        assertEquals(methodId, command.paymentMethodId());
        assertEquals(amount, command.amount());
    }
}
