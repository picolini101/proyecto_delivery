package org.jala.edu.order.application.port.out;

/**
 * Resultado de pedir la autorizacion de un pago, expresado en el lenguaje del modulo de pedidos.
 * <p>
 * Es una interfaz {@code sealed} (sellada): solo puede tener los tres resultados de abajo y ninguno mas.
 * Asi, quien la use esta obligado a pensar en los tres casos de la HU-10: aprobado, rechazado y servicio caido.
 * El puerto {@link PaymentGatewayPort} la devuelve siempre; nunca lanza excepciones por fallas del servicio.
 */
public sealed interface PaymentAuthorization {

    /**
     * Mensaje para mostrar al cliente cuando el pago no se pudo completar.
     *
     * @return el motivo del rechazo o de la falla; texto vacio si el pago fue aprobado
     */
    String message();

    /**
     * El servicio de pagos aprobo el monto. El sistema puede continuar con la confirmacion del pedido.
     *
     * @param paymentReference referencia que entrega el servicio de pagos para este cobro
     */
    record Approved(String paymentReference) implements PaymentAuthorization {

        /** @return texto vacio, porque un pago aprobado no tiene motivo de error */
        @Override
        public String message() {
            return "";
        }
    }

    /**
     * El servicio de pagos respondio, pero NO aprobo el pago (caso A4).
     * El sistema no continua con el pedido y notifica el motivo al cliente.
     *
     * @param paymentReference referencia que entrego el servicio de pagos
     * @param reason           motivo del rechazo, listo para mostrar al cliente
     */
    record Rejected(String paymentReference, String reason) implements PaymentAuthorization {

        /** @return el motivo del rechazo */
        @Override
        public String message() {
            return reason;
        }
    }

    /**
     * El servicio de pagos no respondio o devolvio un error, por eso no se sabe si el pago fue aprobado.
     * El sistema no continua con el pedido y muestra el motivo al cliente.
     *
     * @param reason motivo de la falla, listo para mostrar al cliente
     */
    record Unavailable(String reason) implements PaymentAuthorization {

        /** @return el motivo de la falla */
        @Override
        public String message() {
            return reason;
        }
    }
}
