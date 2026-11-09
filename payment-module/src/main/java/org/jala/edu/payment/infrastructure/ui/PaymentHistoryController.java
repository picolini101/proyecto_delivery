package org.jala.edu.payment.infrastructure.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * Controlador JavaFX para consultar el historial de pagos y prevenir el doble cobro.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentHistoryController {
    @FXML
    private TextField orderIdField;
    @FXML
    private TextField amountField;
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> historyListView;
    private final ManagePaymentHistoryUseCase useCase;
    private final CurrentUserService currentUserService;
    public PaymentHistoryController(ManagePaymentHistoryUseCase useCase,
                                    CurrentUserService currentUserService) {
        this.useCase = useCase;
        this.currentUserService = currentUserService;
    }
    @FXML
    public void initialize() {
        orderIdField.setText(UUID.randomUUID().toString());
        amountField.setText("120.00");
        loadHistory();
    }
    @FXML
    private void handleRecordPayment() {
        try {
            UUID orderId = UUID.fromString(orderIdField.getText().trim());
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            UUID userId = resolveCurrentUserId();
            PaymentTransaction transaction = useCase.recordPaymentAttempt(
                    orderId, userId, SamplePaymentMethods.CUSTOMER_CARD, amount);
            if (transaction.isApproved()) {
                statusLabel.setText("Pago APROBADO para el pedido: " + orderId);
            } else {
                statusLabel.setText("Pago RECHAZADO: " + transaction.getReason());
            }
            loadHistory();
        } catch (IllegalArgumentException | DomainException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    @FXML
    private void handleNewOrderId() {
        orderIdField.setText(UUID.randomUUID().toString());
        statusLabel.setText("Nuevo ID de pedido generado.");
    }
    private void loadHistory() {
        historyListView.getItems().clear();
        try {
            List<PaymentTransaction> transactions = useCase.findByUserId(resolveCurrentUserId());
            for (PaymentTransaction tx : transactions) {
                String line = "Pedido: " + tx.getOrderId()
                        + " | Monto: $" + tx.getAmount()
                        + " | Estado: " + tx.getStatus();
                if (!tx.getReason().isEmpty()) {
                    line += " (" + tx.getReason() + ")";
                }
                historyListView.getItems().add(line);
            }
        } catch (DomainException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    private UUID resolveCurrentUserId() {
        return currentUserService.findCurrentUser()
                .map(CurrentUser::id)
                .orElse(SampleUsers.CUSTOMER);
    }
}