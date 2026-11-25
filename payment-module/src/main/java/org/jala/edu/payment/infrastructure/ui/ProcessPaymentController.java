package org.jala.edu.payment.infrastructure.ui;

import java.math.BigDecimal;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.payment.application.usecase.ManagePaymentMethodUseCase;
import org.jala.edu.payment.domain.model.PaymentMethod;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessPaymentController {

    @FXML
    private TextField orderIdField;

    @FXML
    private ComboBox<PaymentMethod> paymentMethodCombo;

    @FXML
    private TextField amountField;

    @FXML
    private Label resultLabel;

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;
    private final ManagePaymentMethodUseCase paymentMethodUseCase;

    public ProcessPaymentController(
            PaymentService paymentService,
            CurrentUserService currentUserService,
            ManagePaymentMethodUseCase paymentMethodUseCase) {

        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
        this.paymentMethodUseCase = paymentMethodUseCase;
    }

    @FXML
    public void initialize() {
        paymentMethodCombo.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(PaymentMethod method, boolean empty) {
                super.updateItem(method, empty);
                setText(empty || method == null
                        ? null : describe(method));
            }
        });

        paymentMethodCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PaymentMethod method, boolean empty) {
                super.updateItem(method, empty);
                setText(empty || method == null
                        ? null : describe(method));
            }
        });

        loadPaymentMethods();
    }

    private String describe(PaymentMethod method) {
        return method.getAlias().getValue()
                + " (" + method.getType() + ") - "
                + method.getMaskedNumber();
    }

    @FXML
    private void loadPaymentMethods() {
        try {
            UUID userId = currentUserService.currentUser().id();

            paymentMethodCombo.getItems().setAll(
                    paymentMethodUseCase.listPaymentMethods(userId));
            paymentMethodCombo.getSelectionModel().clearSelection();

            if (paymentMethodCombo.getItems().isEmpty()) {
                resultLabel.setText("No tienes metodos de pago registrados.");
            } else {
                resultLabel.setText("Selecciona un metodo de pago registrado.");
            }
        } catch (RuntimeException exception) {
            resultLabel.setText("Error al cargar metodos: " + exception.getMessage());
        }
    }

    @FXML
    private void processPayment() {
        try {
            PaymentMethod selected = paymentMethodCombo.getValue();

            if (selected == null) {
                resultLabel.setText("Selecciona un metodo de pago registrado.");
                return;
            }

            if (orderIdField.getText().isBlank()) {
                resultLabel.setText("Ingresa el ID del pedido.");
                return;
            }

            if (amountField.getText().isBlank()) {
                resultLabel.setText("Ingresa el monto del pedido.");
                return;
            }

            UUID userId = currentUserService.currentUser().id();
            UUID orderId = UUID.fromString(orderIdField.getText().trim());
            UUID paymentMethodId = selected.getId();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            PaymentResult result = paymentService.pay(
                    orderId, userId, paymentMethodId, amount);
            displayPaymentResult(result);
        } catch (IllegalArgumentException exception) {
            resultLabel.setText("Error: Verifica el ID del pedido y el monto.");
        } catch (RuntimeException exception) {
            resultLabel.setText("Error: " + exception.getMessage());
        }
    }

    private void displayPaymentResult(PaymentResult result) {
        if (result.approved()) {
            resultLabel.setText("APROBADO - Referencia: " + result.paymentReference());
        } else {
            resultLabel.setText("RECHAZADO - " + result.reason());
        }
    }
}