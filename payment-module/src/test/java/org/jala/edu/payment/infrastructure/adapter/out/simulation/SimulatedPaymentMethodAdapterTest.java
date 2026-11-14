
package org.jala.edu.payment.infrastructure.adapter.out.simulation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimulatedPaymentMethodAdapterTest {

    private PaymentMethodRepositoryPort repository;
    private SimulatedPaymentMethodAdapter adapter;

    private UUID userId;
    private UUID methodId;

    @BeforeEach
    void setUp() {
        repository = mock(PaymentMethodRepositoryPort.class);
        adapter = new SimulatedPaymentMethodAdapter(repository);

        userId = UUID.randomUUID();
        methodId = UUID.randomUUID();
    }

    @Test
    void shouldAcceptActiveMethodOwnedByUser() {
        PaymentMethod method = mock(PaymentMethod.class);

        when(repository.findById(methodId))
                .thenReturn(Optional.of(method));
        when(method.getUserId()).thenReturn(userId);
        when(method.isActive()).thenReturn(true);

        assertTrue(adapter.isValidPaymentMethod(userId, methodId));
    }

    @Test
    void shouldRejectNonexistentMethod() {
        when(repository.findById(methodId))
                .thenReturn(Optional.empty());

        assertFalse(adapter.isValidPaymentMethod(userId, methodId));
    }

    @Test
    void shouldRejectInactiveMethod() {
        PaymentMethod method = mock(PaymentMethod.class);

        when(repository.findById(methodId))
                .thenReturn(Optional.of(method));
        when(method.getUserId()).thenReturn(userId);
        when(method.isActive()).thenReturn(false);

        assertFalse(adapter.isValidPaymentMethod(userId, methodId));
    }

    @Test
    void shouldRejectMethodOwnedByAnotherUser() {
        PaymentMethod method = mock(PaymentMethod.class);

        when(repository.findById(methodId))
                .thenReturn(Optional.of(method));
        when(method.getUserId()).thenReturn(UUID.randomUUID());

        assertFalse(adapter.isValidPaymentMethod(userId, methodId));
    }
}
