package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.function.Consumer;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputAddressController {
    @FXML private TextField ciudadField;
    @FXML private TextField avenidaField;
    @FXML private TextField numeroCasaField;
    @FXML private Button confirmButton;

    private Consumer<DeliveryAddress> onConfirmed = ignored -> { };

    @FXML
    void initialize() {
        confirmButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> blank(ciudadField) || blank(avenidaField) || blank(numeroCasaField),
                ciudadField.textProperty(), avenidaField.textProperty(), numeroCasaField.textProperty()));
    }

    public void prepare(DeliveryAddress existing, Consumer<DeliveryAddress> onConfirmed) {
        this.onConfirmed = onConfirmed;
        if (existing != null) {
            ciudadField.setText(existing.city());
            avenidaField.setText(existing.avenue());
            numeroCasaField.setText(existing.houseNumber());
        }
    }

    @FXML
    void onConfirm() {
        DeliveryAddress address = new DeliveryAddress(
                ciudadField.getText(), avenidaField.getText(), numeroCasaField.getText());
        onConfirmed.accept(address);
        Dialogs.close(confirmButton);
    }

    @FXML
    void onCancel() {
        Dialogs.close(ciudadField);
    }

    private static boolean blank(TextField field) {
        return field.getText() == null || field.getText().isBlank();
    }
}
