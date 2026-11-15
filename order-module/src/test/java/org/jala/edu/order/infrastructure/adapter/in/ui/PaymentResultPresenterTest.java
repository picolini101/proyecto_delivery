package org.jala.edu.order.infrastructure.adapter.in.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link PaymentResultPresenter} (HU "Mostrar la interfaz de respuesta de pago").
 * Como el presenter no usa JavaFX, se prueba sin abrir ventanas: se comprueba que texto veria el cliente.
 */
class PaymentResultPresenterTest {

    /** Monto de ejemplo del pedido. */
    private static final BigDecimal AMOUNT = new BigDecimal("110.00");

    /** Clase que se esta probando. */
    private PaymentResultPresenter presenter;

    /** Se ejecuta antes de cada prueba. */
    @BeforeEach
    void setUp() {
        presenter = new PaymentResultPresenter();
    }

    /** Arma un intento de pago con la respuesta indicada. */
    private PaymentAttempt attempt(PaymentAuthorization authorization) {
        return new PaymentAttempt(UUID.randomUUID(), AMOUNT, authorization);
    }

    /** Criterio 1: pago aprobado, se muestra exito con el monto y se permite continuar. */
    @Test
    void approvedPaymentShowsSuccessWithTheAmount() {
        PaymentResultView view = presenter.present(attempt(new PaymentAuthorization.Approved("REF-1")));

        assertTrue(view.isSuccess());
        assertEquals(PaymentResultView.Kind.SUCCESS, view.kind());
        assertEquals(PaymentResultPresenter.APPROVED_TITLE, view.title());
        assertTrue(view.message().contains("110.00$"));
        assertTrue(view.message().contains("aceptado"));
    }

    /** Criterio 2: pago rechazado, se muestra un error claro con el motivo del servicio. */
    @Test
    void rejectedPaymentShowsErrorWithTheReason() {
        PaymentResultView view = presenter.present(
                attempt(new PaymentAuthorization.Rejected("REF-2", "Fondos insuficientes")));

        assertFalse(view.isSuccess());
        assertEquals(PaymentResultView.Kind.REJECTED, view.kind());
        assertEquals(PaymentResultPresenter.REJECTED_TITLE, view.title());
        assertTrue(view.message().contains("no pudo procesarse"));
        assertTrue(view.message().contains("Motivo: Fondos insuficientes"));
    }

    /** Si el servicio no entrega motivo, no se muestra la palabra "Motivo". */
    @Test
    void rejectedPaymentWithoutReasonHasNoReasonText() {
        PaymentResultView view = presenter.present(attempt(new PaymentAuthorization.Rejected("REF-3", "  ")));

        assertEquals(PaymentResultView.Kind.REJECTED, view.kind());
        assertTrue(view.message().contains("no pudo procesarse"));
        assertFalse(view.message().contains("Motivo"));
    }

    /** Criterio 3: servicio caido, mensaje amigable y fijo, sin el texto tecnico que llegue del adaptador. */
    @Test
    void unavailableServiceShowsFriendlyMessageWithoutTechnicalDetails() {
        PaymentResultView view = presenter.present(
                attempt(new PaymentAuthorization.Unavailable("java.net.SocketTimeoutException: Read timed out")));

        assertEquals(PaymentResultView.Kind.UNAVAILABLE, view.kind());
        assertEquals(PaymentResultPresenter.UNAVAILABLE_MESSAGE, view.message());
        assertFalse(view.message().contains("Exception"));
        assertFalse(view.message().contains("timed out"));
    }

    /** Un error inesperado del caso de uso tampoco expone su mensaje ni su tipo. */
    @Test
    void unexpectedErrorShowsFriendlyMessageWithoutExposingTheException() {
        PaymentResultView view = presenter.presentFailure(
                new IllegalStateException("NullPointerException at AuthorizePaymentUseCase.java:42"));

        assertEquals(PaymentResultView.Kind.UNAVAILABLE, view.kind());
        assertEquals(PaymentResultPresenter.UNAVAILABLE_MESSAGE, view.message());
        assertFalse(view.message().contains("NullPointer"));
        assertFalse(view.message().contains("AuthorizePaymentUseCase"));
    }
}
