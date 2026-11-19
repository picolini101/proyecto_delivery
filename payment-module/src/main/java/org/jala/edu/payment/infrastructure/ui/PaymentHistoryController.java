package org.jala.edu.payment.infrastructure.ui;

import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.usecase.ManagePaymentHistoryUseCase;
import org.jala.edu.payment.domain.model.PaymentTransaction;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * Controlador JavaFX para consultar y actualizar el historial de pagos del cliente.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentHistoryController {
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
        loadHistory();
        historyListView.sceneProperty().addListener(obs -> connectTabRefresh());
    }
    @FXML
    private void handleRefreshHistory() {
        loadHistory();
        statusLabel.setText("Historial actualizado.");
    }
    private void connectTabRefresh() {
        Parent current = historyListView.getParent();
        while (current != null) {
            if (current instanceof TabPane tabPane) {
                tabPane.getSelectionModel().selectedItemProperty().addListener(obs -> loadHistory());
                return;
            }
            current = current.getParent();
        }
    }
    /**
     * Recarga las transacciones del usuario activo en la lista del historial.
     */
    @FXML
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