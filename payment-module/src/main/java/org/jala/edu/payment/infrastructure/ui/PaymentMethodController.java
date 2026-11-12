package org.jala.edu.payment.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.in.RegisterPaymentMethodCommand;
import org.jala.edu.payment.application.usecase.ManagePaymentMethodUseCase;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.jala.edu.core.contract.CurrentUser;
import java.util.Optional;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentMethodController {

    @FXML private TextField aliasField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField numberField;
    @FXML private Label statusLabel;
    @FXML private ListView<String> paymentListView;

    private final ManagePaymentMethodUseCase useCase;
    private final CurrentUserService currentUserService;

    public PaymentMethodController(ManagePaymentMethodUseCase useCase,
                                   CurrentUserService currentUserService) {
        this.useCase = useCase;
        this.currentUserService = currentUserService;
    }

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("TARJETA", "TRANSFERENCIA");
        loadPaymentMethods();
    }

    @FXML
    private void handleSave() {
        try {
            // El usuario se lee al pulsar el botón, no en una constante ni en el constructor
            Optional<UUID> userId = sessionUserId();
            if (userId.isEmpty()) {
                return;
            }
            RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                    userId.get(),
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
        try {
            Optional<UUID> userId = sessionUserId();
            if (userId.isEmpty()) {
                return;
            }
            for (PaymentMethod pm : useCase.listPaymentMethods(userId.get())) {
                String item = pm.getAlias().getValue() + " (" + pm.getType() + ") - " + pm.getMaskedNumber();
                if (pm.isLastRegistered()) {
                    item += " [Último Registrado]";
                }
                paymentListView.getItems().add(item);
            }
        } catch (DomainException e) {
            // Usuario sin rol CUSTOMER.

            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        aliasField.clear();
        numberField.clear();
    }

    // Devuelve el id del usuario en sesión; si no hay sesión, lo informa en pantalla y devuelve vacío
    private Optional<UUID> sessionUserId() {
        Optional<UUID> userId = currentUserService.findCurrentUser().map(CurrentUser::id);
        if (userId.isEmpty()) {
            statusLabel.setText("Error: No hay una sesión activa. Inicia sesión de nuevo.");
        }
        return userId;
    }
}