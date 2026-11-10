package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.delivery.application.port.in.FindCustomerShipmentQuery;
import org.jala.edu.delivery.application.usecase.FindCustomerShipmentUseCase;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Read-only view of the signed-in customer's shipment (view: {@code customer-shipment.fxml}).
 * The confirmation code is shown only while the courier still has to close the delivery.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CustomerShipmentController {

    private final FindCustomerShipmentUseCase findCustomerShipmentUseCase;

    @FXML
    private Label messageLabel;

    @FXML
    private Label emptyLabel;

    @FXML
    private VBox progressPanel;

    @FXML
    private Label progressLabel;

    @FXML
    private Label codeLabel;

    @FXML
    private Button refreshButton;

    @FXML
    void initialize() {
        reload();
    }

    @FXML
    void onRefresh() {
        reload();
    }

    private void reload() {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> findCustomerShipmentUseCase.execute(new FindCustomerShipmentQuery()),
                delivery -> {
                    show(delivery.orElse(null));
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the shipment"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private void show(ActiveDelivery delivery) {
        boolean present = delivery != null;
        emptyLabel.setVisible(!present);
        emptyLabel.setManaged(!present);
        progressPanel.setVisible(present);
        progressPanel.setManaged(present);
        if (!present) {
            return;
        }
        progressLabel.setText("Progreso: " + label(delivery.step()));
        boolean showCode = delivery.isInProgress();
        codeLabel.setVisible(showCode);
        codeLabel.setManaged(showCode);
        if (showCode) {
            codeLabel.setText("Código de confirmación: " + delivery.confirmationCode().value());
        }
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String label(DeliveryStep step) {
        return switch (step) {
            case ON_THE_WAY -> "En camino";
            case PICKED_UP -> "Retirado";
            case DELIVERED -> "Entregado";
            case CANCELLED -> "Cancelado";
            case DELEGATED -> "Delegado";
        };
    }
}
