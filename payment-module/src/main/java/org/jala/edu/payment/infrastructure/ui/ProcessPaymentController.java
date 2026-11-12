package org.jala.edu.payment.infrastructure.ui;

import java.math.BigDecimal;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.PaymentService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProcessPaymentController {

    @FXML private TextField orderIdField;
    @FXML private TextField paymentMethodIdField;
    @FXML private TextField amountField;
    @FXML private Label resultLabel;

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    public ProcessPaymentController(
            PaymentService paymentService,
            CurrentUserService currentUserService) {
        this.paymentService = paymentService;
        this.currentUserService = currentUserService;
    }

    @FXML
    private void processPayment() {
        try {
            UUID userId = currentUserService.currentUser().id();
            UUID orderId = UUID.fromString(orderIdField.getText().trim());
            UUID paymentMethodId = UUID.fromString(
                    paymentMethodIdField.getText().trim());
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            PaymentResult result = paymentService.pay(
                    orderId, userId, paymentMethodId, amount);

            if (result.approved()) {
                resultLabel.setText(
                        "APROBADO - Referencia: " + result.paymentReference());
            } else {
                resultLabel.setText("RECHAZADO - " + result.reason());
            }
        } catch (RuntimeException exception) {
            resultLabel.setText("Error: " + exception.getMessage());
        }
    }
}
