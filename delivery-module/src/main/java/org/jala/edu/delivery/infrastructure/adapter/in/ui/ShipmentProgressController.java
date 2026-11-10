package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.delivery.application.port.in.CancelDeliveryCommand;
import org.jala.edu.delivery.application.port.in.ConfirmDeliveryCommand;
import org.jala.edu.delivery.application.port.in.DelegateDeliveryCommand;
import org.jala.edu.delivery.application.port.in.FindActiveDeliveryQuery;
import org.jala.edu.delivery.application.port.in.PickUpDeliveryCommand;
import org.jala.edu.delivery.application.usecase.CancelDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.ConfirmDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.DelegateDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.FindActiveDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.PickUpDeliveryUseCase;
import org.jala.edu.delivery.domain.model.ActiveDelivery;
import org.jala.edu.delivery.domain.model.DeliveryStep;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Sequential controls for the courier's shipment in progress (view: {@code shipment-progress.fxml}).
 * The controls stay hidden until that courier has a shipment in progress.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ShipmentProgressController {

    private final FindActiveDeliveryUseCase findActiveDeliveryUseCase;
    private final PickUpDeliveryUseCase pickUpDeliveryUseCase;
    private final ConfirmDeliveryUseCase confirmDeliveryUseCase;
    private final CancelDeliveryUseCase cancelDeliveryUseCase;
    private final DelegateDeliveryUseCase delegateDeliveryUseCase;

    @FXML
    private Label messageLabel;

    @FXML
    private Label emptyLabel;

    @FXML
    private VBox progressPanel;

    @FXML
    private Label progressLabel;

    @FXML
    private Button pickUpButton;

    @FXML
    private TextField codeField;

    @FXML
    private Button deliverButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button delegateButton;

    @FXML
    void initialize() {
        reload(null);
    }

    @FXML
    void onPickUp() {
        run(() -> pickUpDeliveryUseCase.execute(new PickUpDeliveryCommand()), "Pedido retirado.");
    }

    @FXML
    void onDeliver() {
        String code = codeField.getText();
        run(() -> confirmDeliveryUseCase.execute(new ConfirmDeliveryCommand(code)), "Pedido entregado.");
    }

    @FXML
    void onCancel() {
        run(() -> cancelDeliveryUseCase.execute(new CancelDeliveryCommand()), "Envío cancelado.");
    }

    @FXML
    void onDelegate() {
        run(() -> delegateDeliveryUseCase.execute(new DelegateDeliveryCommand()), "Envío delegado.");
    }

    private void run(java.util.concurrent.Callable<?> work, String successMessage) {
        setBusy(true);
        BackgroundTasks.run(work::call, done -> reload(successMessage), error -> {
            showMessage(ErrorMessages.of(error, "update the shipment"), "error");
            setBusy(false);
        });
    }

    private void reload(String successMessage) {
        setBusy(true);
        BackgroundTasks.run(
                () -> findActiveDeliveryUseCase.execute(new FindActiveDeliveryQuery()),
                delivery -> {
                    setBusy(false);
                    show(delivery.orElse(null));
                    if (successMessage != null) {
                        showMessage(successMessage, "success");
                    }
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the shipment"), "error");
                    setBusy(false);
                });
    }

    private void show(ActiveDelivery delivery) {
        boolean inProgress = delivery != null;
        emptyLabel.setVisible(!inProgress);
        emptyLabel.setManaged(!inProgress);
        progressPanel.setVisible(inProgress);
        progressPanel.setManaged(inProgress);
        if (!inProgress) {
            return;
        }
        progressLabel.setText("Progreso: " + label(delivery.step()));
        boolean onTheWay = delivery.step() == DeliveryStep.ON_THE_WAY;
        boolean pickedUp = delivery.step() == DeliveryStep.PICKED_UP;
        pickUpButton.setDisable(!onTheWay);
        codeField.setDisable(!pickedUp);
        deliverButton.setDisable(!pickedUp);
        cancelButton.setDisable(false);
        delegateButton.setDisable(false);
    }

    private void setBusy(boolean busy) {
        progressPanel.setDisable(busy);
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
