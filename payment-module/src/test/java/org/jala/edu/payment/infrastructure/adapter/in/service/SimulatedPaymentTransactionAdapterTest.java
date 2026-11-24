package org.jala.edu.payment.infrastructure.adapter.in.service;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.jala.edu.payment.infrastructure.adapter.out.simulation.SimulatedPaymentTransactionAdapter;
import org.jala.edu.payment.infrastructure.persistence.adapter.InMemoryPaymentTransactionRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulatedPaymentTransactionAdapterTest {

    private InMemoryPaymentTransactionRepositoryAdapter repository;
    private SimulatedPaymentTransactionAdapter adapter;

    private UUID orderId;
    private UUID userId;
    private UUID paymentMethodId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPaymentTransactionRepositoryAdapter();

        UserService userService = mock(UserService.class);
        userId = UUID.randomUUID();

        when(userService.isActiveWithRole(
                userId, UserRole.CUSTOMER)).thenReturn(true);

        ManagePaymentHistoryUseCase historyUseCase =
                new ManagePaymentHistoryUseCase(repository, userService);

        adapter = new SimulatedPaymentTransactionAdapter(
                historyUseCase, repository);

        orderId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
    }

    @Test
    void shouldSaveApprovedPaymentInHistory() {
        String reference = adapter.recordApproved(
                orderId, userId, paymentMethodId,
                new BigDecimal("100.00"));

        assertNotNull(reference);
        assertFalse(reference.isBlank());

        var transactions = repository.findByUserId(userId);

        assertEquals(1, transactions.size());
        assertTrue(transactions.get(0).isApproved());
        assertEquals(reference,
                transactions.get(0).getId().value().toString());
    }

    @Test
    void shouldRejectDuplicatePayment() {
        adapter.recordApproved(
                orderId, userId, paymentMethodId,
                new BigDecimal("100.00"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> adapter.recordApproved(
                        orderId, userId, paymentMethodId,
                        new BigDecimal("100.00")));

        assertEquals(
                "Intento de cobro duplicado sobre pedido aprobado",
                exception.getMessage());

        var transactions = repository.findByUserId(userId);

        assertEquals(2, transactions.size());
        assertEquals(1, transactions.stream()
                .filter(PaymentTransaction::isApproved)
                .count());
    }

    @Test
    void shouldSaveRejectedPaymentInHistory() {
        adapter.recordRejected(
                orderId, userId, paymentMethodId,
                new BigDecimal("100.00"),
                "Metodo de pago invalido o inactivo");

        var transactions = repository.findByUserId(userId);

        assertEquals(1, transactions.size());
        assertFalse(transactions.get(0).isApproved());
        assertEquals(
                "Metodo de pago invalido o inactivo",
                transactions.get(0).getReason());
    }
}
