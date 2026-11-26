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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.TextField;
import org.jala.edu.payment.application.dto.PaymentStatusResult;

// Controlador JavaFX para consultar y actualizar el historial de pagos del cliente.

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PaymentHistoryController {
    @FXML
    private Label statusLabel;
    @FXML
    private ListView<String> historyListView;
    @FXML
    private TextField statusOrderIdField;
    @FXML
    private Label statusResultLabel;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
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
    // Recarga las transacciones del usuario activo en la lista del historial.

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

     // Consulta el estado de pago del pedido escrito por el cliente (HU #31).

    @FXML
    private void handleCheckStatus() {
        String text = statusOrderIdField.getText().trim();
        if (text.isEmpty()) {
            statusResultLabel.setText("Ingresa el ID del pedido.");
            return;
        }
        try {
            PaymentStatusResult result = useCase.findPaymentStatus(UUID.fromString(text), resolveCurrentUserId());
            statusResultLabel.setText(describeStatus(result));
        } catch (IllegalArgumentException e) {
            statusResultLabel.setText("Error: el ID del pedido no es valido.");
        } catch (DomainException e) {
            statusResultLabel.setText("Error: " + e.getMessage());
        }
    }

    private String describeStatus(PaymentStatusResult result) {
        if (PaymentStatusResult.PENDING.equals(result.status())) {
            return "Estado: PENDIENTE (el pedido aun no tiene intentos de cobro)";
        }
        return "Estado: " + result.status()
                + " | Monto: $" + result.amount()
                + " | Fecha: " + DATE_FORMAT.format(result.attemptedAt());
    }

    private UUID resolveCurrentUserId() {
        return currentUserService.findCurrentUser()
                .map(CurrentUser::id)
                .orElse(SampleUsers.CUSTOMER);
    }
}