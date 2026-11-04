package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import java.util.UUID;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import lombok.RequiredArgsConstructor;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.delivery.application.port.in.ListCouriersQuery;
import org.jala.edu.delivery.application.port.in.RegisterCourierCommand;
import org.jala.edu.delivery.application.usecase.ListCouriersUseCase;
import org.jala.edu.delivery.application.usecase.RegisterCourierUseCase;
import org.jala.edu.delivery.domain.model.Courier;
import org.jala.edu.delivery.domain.model.TransportMode;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller for the delivery driver registration screen
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CourierRegistrationController {

    // Test admin user that already exist in the project
    private static final UUID CURRENT_ADMIN = SampleUsers.ADMIN;

    private final RegisterCourierUseCase registerCourier;
    private final ListCouriersUseCase listCouriers;

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<TransportMode> transportModeComboBox;

    @FXML private Label nameErrorLabel;
    @FXML private Label phoneErrorLabel;
    @FXML private Label transportErrorLabel;
    @FXML private Label statusLabel;

    @FXML private Button registerButton;
    @FXML private ProgressIndicator spinner;

    @FXML private TableView<Courier> couriersTable;

    @FXML private TableColumn<Courier, String> nameColumn;
    @FXML private TableColumn<Courier, String> phoneColumn;
    @FXML private TableColumn<Courier, String> transportColumn;
    @FXML private TableColumn<Courier, String> roleColumn;

    private boolean nameTouched;
    private boolean phoneTouched;
    private boolean transportTouched;
    private boolean submitted;

    @FXML
    void initialize() {

        // Transportation mode options
        transportModeComboBox.getItems().setAll(TransportMode.values());

        // Table columns configuration
        nameColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().getName()));

        phoneColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().getPhoneNumber()));

        transportColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(
                        data.getValue().getTransportMode().toString()));

        roleColumn.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(
                        data.getValue().getRole().name()));

        // Real-time validation
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            nameTouched = true;
            updateValidation(false);
        });

        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            phoneTouched = true;
            updateValidation(false);
        });

        transportModeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            transportTouched = true;
            updateValidation(false);
        });

        hideSpinner();
        refreshTable();
        updateValidation(false);
    }

    /**
     * Executed when the Register Delivery Driver button is pressed
     */
    @FXML
    void onRegister() {

        submitted = true;

        // Nothing is sent if there are errors
        if (!updateValidation(true)) {
            showStatus(
                    "Revisa los campos marcados antes de continuar",
                    "error"
            );
            return;
        }

        RegisterCourierCommand command = new RegisterCourierCommand(
                CURRENT_ADMIN,
                nameField.getText().strip(),
                phoneField.getText().strip(),
                transportModeComboBox.getValue()
        );

        showStatus("Registrando repartidor...", "info");
        showSpinner();

        // Run the registration in the background to avoid freezing the interfac
        BackgroundTasks.run(
                () -> registerCourier.execute(command),

                courier -> {
                    hideSpinner();

                    showStatus(
                            "Repartidor registrado. Rol asignado: "
                                    + courier.getRole().name(),
                            "success"
                    );

                    refreshTable();
                    clearForm();
                },

                error -> {
                    hideSpinner();

                    showStatus(
                            ErrorMessages.of(error, "registrar el repartidor"),
                            "error"
                    );
                }
        );
    }

    /**
     * Checks the data and displays the corresponding errors
     */
    private boolean updateValidation(boolean showAllErrors) {

        String name = nameField.getText();
        String phone = phoneField.getText();
        TransportMode transport = transportModeComboBox.getValue();

        boolean validName = name != null && !name.isBlank();
        boolean validPhone = Courier.isValidPhoneNumber(phone);
        boolean validTransport = transport != null;

        showFieldError(
                nameField,
                nameErrorLabel,
                validName,
                showAllErrors || submitted || nameTouched,
                "El nombre es obligatorio"
        );

        showFieldError(
                phoneField,
                phoneErrorLabel,
                validPhone,
                showAllErrors || submitted || phoneTouched,
                "Ingresa un telefono válido (7 a 15 dígitos, opcionalmente con + al inicio)"
        );

        showFieldError(
                transportModeComboBox,
                transportErrorLabel,
                validTransport,
                showAllErrors || submitted || transportTouched,
                "Selecciona un medio de transporte"
        );

        return validName && validPhone && validTransport;
    }

    /**
     * Adds a red border and message when a field is invalid
     */
    private void showFieldError(
            Control field,
            Label errorLabel,
            boolean valid,
            boolean show,
            String message) {

        boolean hasError = show && !valid;

        field.setStyle(
                hasError
                        ? "-fx-border-color: #dc2626; -fx-border-width: 1.5;"
                        : ""
        );

        errorLabel.setText(hasError ? message : "");
        errorLabel.setVisible(hasError);
        errorLabel.setManaged(hasError);
    }

    /**
     * Updates the table with the saved delivery drivers
     */
    private void refreshTable() {

        couriersTable.setItems(
                FXCollections.observableArrayList(
                        listCouriers.execute(new ListCouriersQuery())
                )
        );
    }

    /**
     * Clears the form after registering
     */
    private void clearForm() {

        nameField.clear();
        phoneField.clear();
        transportModeComboBox.getSelectionModel().clearSelection();

        nameTouched = false;
        phoneTouched = false;
        transportTouched = false;
        submitted = false;

        updateValidation(false);
    }

    private void showSpinner() {

        spinner.setVisible(true);
        spinner.setManaged(true);
        registerButton.setDisable(true);
    }

    private void hideSpinner() {

        spinner.setVisible(false);
        spinner.setManaged(false);
        registerButton.setDisable(false);
    }

    private void showStatus(String message, String type) {

        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("info", "success", "error");
        statusLabel.getStyleClass().add(type);
    }
}