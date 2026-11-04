package org.jala.edu.payment.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jala.edu.payment.application.dto.RegisterPaymentMethodCommand;
import org.jala.edu.payment.application.usecase.ManagePaymentMethodUseCase;
import org.jala.edu.payment.domain.exception.DomainException;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentMethodController {

    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @FXML private TextField aliasField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField numberField;
    @FXML private Label statusLabel;
    @FXML private ListView<String> paymentListView;

    private final ManagePaymentMethodUseCase useCase;
    private UUID currentUserId;

    // Inyección de dependencias
    public PaymentMethodController(ManagePaymentMethodUseCase useCase) {
        this.useCase = useCase;
    }

    @FXML
    public void initialize() {
        // Cargar opciones en el ComboBox
        typeComboBox.getItems().addAll("TARJETA", "TRANSFERENCIA");

        if (this.currentUserId == null) {
            this.currentUserId = DEFAULT_USER_ID;
        }
    }

    public void setUserId(UUID userId) {
        this.currentUserId = userId;
        loadPaymentMethods();
    }

    @FXML
    private void handleSave() {
        try {
            RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                    currentUserId,
                    aliasField.getText(),
                    typeComboBox.getValue(),
                    numberField.getText()
            );

            useCase.registerPaymentMethod(command);
            statusLabel.setText("Método guardado exitosamente");
            clearFields();
            loadPaymentMethods();
        } catch (DomainException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void loadPaymentMethods() {
        paymentListView.getItems().clear();
        if (currentUserId != null) {
            for (PaymentMethod pm : useCase.listPaymentMethods(currentUserId)) {
                String item = pm.getAlias().getValue() + " (" + pm.getType() + ") - " + pm.getMaskedNumber();
                if (pm.isLastRegistered()) {
                    item += " [Último Registrado]";
                }
                paymentListView.getItems().add(item);
            }
        }
    }

    private void clearFields() {
        aliasField.clear();
        numberField.clear();
    }
}