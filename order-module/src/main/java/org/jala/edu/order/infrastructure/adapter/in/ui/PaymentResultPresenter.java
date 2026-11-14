package org.jala.edu.order.infrastructure.adapter.in.ui;

import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.out.PaymentAuthorization;

/**
 * Presenter decide QUE texto ve el cliente segun el resultado del pago.
 * <p>
 * No usa JavaFX. Solo recibe el resultado del caso de uso y devuelve un {@link PaymentResultView}.
 * <p>
 */
public class PaymentResultPresenter {

    /** Titulo cuando el pago fue aprobado. */
    static final String APPROVED_TITLE = "Pago aprobado";

    /** Titulo cuando el pago fue rechazado. */
    static final String REJECTED_TITLE = "Pago rechazado";

    /** Titulo cuando hay un problema temporal. */
    static final String UNAVAILABLE_TITLE = "Problema temporal";

    /** Mensaje fijo para problemas temporales; nunca incluye detalles tecnicos. */
    static final String UNAVAILABLE_MESSAGE =
            "Tuvimos un problema temporal al procesar tu pago. Inténtalo de nuevo en unos minutos.";

    /**
     * Convierte el resultado de autorizar el pago en lo que debe mostrarse.
     * El {@code switch} cubre los tres casos de la interfaz sellada; si algun dia se agrega un caso nuevo,
     * el compilador avisa porque este {@code switch} dejaria de ser completo.
     *
     * @param attempt resultado de {@code AuthorizePaymentUseCase}
     * @return la ventana a mostrar: exito, rechazo o problema temporal
     */
    public PaymentResultView present(PaymentAttempt attempt) {
        return switch (attempt.authorization()) {
            case PaymentAuthorization.Approved approved -> new PaymentResultView(
                    PaymentResultView.Kind.SUCCESS,
                    APPROVED_TITLE,
                    "Tu pago por " + attempt.amount() + "$ fue aceptado. Puedes continuar con tu pedido.");
            case PaymentAuthorization.Rejected rejected -> new PaymentResultView(
                    PaymentResultView.Kind.REJECTED,
                    REJECTED_TITLE,
                    rejectedMessage(rejected.reason()));
            case PaymentAuthorization.Unavailable unavailable -> unavailable();
        };
    }

    /**
     * Convierte un error inesperado (por ejemplo, el pedido no se encontro) en un mensaje amigable.
     * El mensaje de la excepcion NO se muestra: puede estar en ingles o ser tecnico.
     *
     * @param error el error que lanzo el caso de uso
     * @return la ventana de problema temporal
     */
    public PaymentResultView presentFailure(Throwable error) {
        return unavailable();
    }

    /** @return la ventana de problema temporal, con el mensaje fijo y amigable */
    private PaymentResultView unavailable() {
        return new PaymentResultView(PaymentResultView.Kind.UNAVAILABLE, UNAVAILABLE_TITLE, UNAVAILABLE_MESSAGE);
    }

    /**
     * Arma el mensaje de rechazo. Agrega el motivo solo si el servicio de pagos lo entrego.
     *
     * @param reason motivo entregado por el servicio de pagos; puede ser nulo o vacio
     * @return mensaje para el cliente
     */
    private String rejectedMessage(String reason) {
        String base = "El pago no pudo procesarse.";
        if (reason != null && !reason.isBlank()) {
            base += " Motivo: " + reason.strip() + ".";
        }
        return base + " Puedes reintentar o corregir tus datos de pago.";
    }
}
