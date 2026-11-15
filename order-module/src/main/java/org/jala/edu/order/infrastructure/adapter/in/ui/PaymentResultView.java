package org.jala.edu.order.infrastructure.adapter.in.ui;

/**
 * Lo que la ventana de respuesta de pago muestra: un tipo, un titulo y un mensaje.
 * <p>
 * Es solo un contenedor de datos (un {@code record}). Lo crea {@link PaymentResultPresenter} y lo lee
 * {@link PaymentResultController}; asi la logica de que texto mostrar queda fuera de JavaFX y se puede probar.
 *
 * @param kind    tipo de resultado: aprobado, rechazado o servicio no disponible
 * @param title   titulo de la ventana
 * @param message mensaje en espanol para el cliente, sin detalles tecnicos
 */
public record PaymentResultView(Kind kind, String title, String message) {

    /** Los tres resultados posibles que ve el cliente. */
    public enum Kind {
        /** El pago fue aprobado. */
        SUCCESS,
        /** El servicio de pagos rechazo el pago  */
        REJECTED,
        /** El servicio de pagos no respondio o fallo. */
        UNAVAILABLE
    }

    /** @return {@code true} si el pago fue aprobado */
    public boolean isSuccess() {
        return kind == Kind.SUCCESS;
    }
}
