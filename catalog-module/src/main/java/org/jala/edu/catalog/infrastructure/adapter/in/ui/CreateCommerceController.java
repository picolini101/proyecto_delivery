package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.port.in.RegisterCommerceCommand;
import org.jala.edu.catalog.application.usecase.RegisterCommerceUseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.contract.UserSummary;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX para «registrar comercio» (vista: {@code create-commerce.fxml}).
 * El administrador elige el comerciante dueño entre los MERCHANT activos (nombre y correo), sin escribir ids.
 * Lee el formulario, invoca el caso de uso y muestra el resultado; las validaciones viven en el dominio.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CreateCommerceController {

    private final RegisterCommerceUseCase registerCommerceUseCase;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    @FXML
    private GridPane form;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<UserSummary> merchantSelector;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        merchantSelector.setConverter(new MerchantConverter());
        loadMerchants();
    }

    @FXML
    void onCreate() {
        UserSummary merchant = merchantSelector.getValue();
        if (merchant == null) {
            MessageLabels.show(messageLabel, "Select the merchant who owns this commerce", "error");
            return;
        }

        // Se leen los campos en el hilo de JavaFX, antes de pasar al hilo de fondo
        String name = nameField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();

        form.setDisable(true);
        messageLabel.setText("");
        BackgroundTasks.run(
                () -> registerCommerceUseCase.execute(new RegisterCommerceCommand(
                        currentUserService.currentUser().id(), name, address, phone, merchant.id())),
                commerceId -> {
                    MessageLabels.show(messageLabel,
                            "Commerce " + name.strip() + " registered as Disabled.", "success");
                    clearForm();
                    form.setDisable(false);
                },
                error -> {
                    MessageLabels.show(messageLabel,
                            ErrorMessages.of(error, "register the commerce"), "error");
                    form.setDisable(false);
                });
    }

    /** Carga los MERCHANT activos (ordenados por nombre) para el selector de dueño. */
    private void loadMerchants() {
        BackgroundTasks.run(
                () -> userService.findActiveByRole(UserRole.MERCHANT),
                merchants -> {
                    merchantSelector.getItems().setAll(merchants);
                    if (merchants.isEmpty()) {
                        MessageLabels.show(messageLabel,
                                "There are no active merchants. Register one in Users first.", "info");
                    }
                },
                error -> MessageLabels.show(messageLabel,
                        ErrorMessages.of(error, "load the merchants"), "error"));
    }

    private void clearForm() {
        nameField.clear();
        addressField.clear();
        phoneField.clear();
        merchantSelector.setValue(null);
        nameField.requestFocus();
    }

    /** Muestra «Nombre Apellido (correo)» de cada comerciante en el selector. */
    private static final class MerchantConverter extends StringConverter<UserSummary> {

        @Override
        public String toString(UserSummary user) {
            return user == null ? "" : user.fullName() + " (" + user.email() + ")";
        }

        @Override
        public UserSummary fromString(String text) {
            return null;
        }
    }
}