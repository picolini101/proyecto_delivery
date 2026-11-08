package org.jala.edu.order.infrastructure.adapter.out.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link PaymentServiceAdapter} (HU-10, INT-002).
 * El servicio de pagos del core es un mock (objeto falso): la prueba decide que responde en cada caso.
 */
class PaymentServiceAdapterTest {

    /** Datos de ejemplo para la solicitud. */
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = new BigDecimal("110.00");

    /** Servicio de pagos falso. */
    private PaymentService paymentService;

    /** Clase que se esta probando. */
    private PaymentServiceAdapter adapter;

    /** Se ejecuta antes de cada prueba. */
    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        adapter = new PaymentServiceAdapter(paymentService);
    }

    /** Llama al adaptador con los datos de ejemplo. */
    private PaymentAuthorization authorize() {
        return adapter.authorize(ORDER_ID, SampleUsers.CUSTOMER, SamplePaymentMethods.CUSTOMER_CARD, AMOUNT);
    }

    /** Criterio 1: el servicio aprueba, el adaptador devuelve Approved con la referencia y envia el monto total. */
    @Test
    void approvedPaymentIsTranslatedToApproved() {
        when(paymentService.pay(ORDER_ID, SampleUsers.CUSTOMER, SamplePaymentMethods.CUSTOMER_CARD, AMOUNT))
                .thenReturn(new PaymentResult("REF-1", true, ""));

        PaymentAuthorization result = authorize();

        PaymentAuthorization.Approved approved = assertInstanceOf(PaymentAuthorization.Approved.class, result);
        assertEquals("REF-1", approved.paymentReference());
        verify(paymentService).pay(ORDER_ID, SampleUsers.CUSTOMER, SamplePaymentMethods.CUSTOMER_CARD, AMOUNT);
    }

    /** Criterio 2: el servicio rechaza, el adaptador devuelve Rejected con el motivo. */
    @Test
    void rejectedPaymentKeepsTheReason() {
        when(paymentService.pay(any(), any(), any(), any()))
                .thenReturn(new PaymentResult("REF-2", false, "Amount exceeds 1000.00"));

        PaymentAuthorization result = authorize();

        PaymentAuthorization.Rejected rejected = assertInstanceOf(PaymentAuthorization.Rejected.class, result);
        assertEquals("REF-2", rejected.paymentReference());
        assertEquals("Amount exceeds 1000.00", rejected.message());
    }

    /** Un rechazo sin motivo recibe un mensaje por defecto, para que el cliente siempre vea algo. */
    @Test
    void rejectedPaymentWithoutReasonGetsDefaultMessage() {
        when(paymentService.pay(any(), any(), any(), any()))
                .thenReturn(new PaymentResult("REF-3", false, ""));

        PaymentAuthorization result = authorize();

        assertEquals(PaymentServiceAdapter.DEFAULT_REJECTION_MESSAGE, result.message());
    }

    /** Criterio 3: el servicio lanza un error, el adaptador devuelve Unavailable y no propaga la excepcion. */
    @Test
    void serviceErrorIsTranslatedToUnavailable() {
        when(paymentService.pay(any(), any(), any(), any())).thenThrow(new IllegalStateException("timeout"));

        PaymentAuthorization result = authorize();

        assertInstanceOf(PaymentAuthorization.Unavailable.class, result);
        assertEquals(PaymentServiceAdapter.SERVICE_UNAVAILABLE_MESSAGE, result.message());
    }

    /** Criterio 3: si el servicio no devuelve nada (null), tambien cuenta como no disponible. */
    @Test
    void nullResponseIsTranslatedToUnavailable() {
        when(paymentService.pay(any(), any(), any(), any())).thenReturn(null);

        assertInstanceOf(PaymentAuthorization.Unavailable.class, authorize());
    }
}
