package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controlador de la ventana de respuesta de pago ({@code payment-result.fxml}).
 * <p>
 * Solo dibuja lo que le dice {@link PaymentResultView} y avisa cuando el cliente pulsa el boton principal.
 * No decide textos (eso lo hace {@link PaymentResultPresenter}) ni llama a casos de uso.
 * Igual que {@code InputAddressController}, es {@code @Scope(PROTOTYPE)}: se crea uno nuevo cada vez que se abre.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentResultController {

    /** Estilo de los botones principales. */
    private static final String PRIMARY_STYLE = "-fx-background-color: %s; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 9 18;";

    /** Icono grande: tilde, cruz o advertencia. */
    @FXML private Label iconLabel;

    /** Titulo del resultado. */
    @FXML private Label titleLabel;

    /** Mensaje para el cliente. */
    @FXML private Label messageLabel;

    /** Boton secundario "Cerrar"; solo se ve cuando el pago no fue aprobado. */
    @FXML private Button closeButton;

    /** Boton principal: "Continuar" si se aprobo, "Reintentar pago" si no. */
    @FXML private Button primaryButton;

    /** Accion que se ejecuta al pulsar el boton principal, despues de cerrar la ventana. */
    private Runnable onPrimary = () -> { };

    /**
     * Prepara la ventana antes de mostrarla.
     *
     * @param view      lo que debe mostrarse
     * @param onPrimary accion del boton principal (ir a pedidos si se aprobo, reintentar el pago si no)
     */
    public void prepare(PaymentResultView view, Runnable onPrimary) {
        this.onPrimary = onPrimary;
        titleLabel.setText(view.title());
        messageLabel.setText(view.message());
        switch (view.kind()) {
            case SUCCESS -> style("✔", "#16a34a", "Continuar", false);
            case REJECTED -> style("✖", "#dc2626", "Reintentar pago", true);
            case UNAVAILABLE -> style("⚠", "#d97706", "Reintentar pago", true);
        }
    }

    /** Aplica el icono, el color y los botones segun el tipo de resultado. */
    private void style(String icon, String color, String primaryText, boolean showClose) {
        iconLabel.setText(icon);
        iconLabel.setStyle("-fx-font-size: 46px; -fx-text-fill: " + color + ";");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        primaryButton.setText(primaryText);
        primaryButton.setStyle(String.format(PRIMARY_STYLE, color));
        closeButton.setVisible(showClose);
        closeButton.setManaged(showClose);
    }

    /** Boton principal: cierra la ventana y ejecuta la accion pedida. */
    @FXML
    void onPrimary() {
        Dialogs.close(primaryButton);
        onPrimary.run();
    }

    /** Boton "Cerrar": solo cierra la ventana; el cliente sigue en el carrito. */
    @FXML
    void onClose() {
        Dialogs.close(closeButton);
    }
}
