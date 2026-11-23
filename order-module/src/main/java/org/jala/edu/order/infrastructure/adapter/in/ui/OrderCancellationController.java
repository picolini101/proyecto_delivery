package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.order.application.port.in.CancelOrderCommand;
import org.jala.edu.order.application.usecase.orders.CancelOrderUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderStatusUseCase;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controlador del panel para listar y cancelar pedidos activos del cliente (HU #71).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderCancellationController {

    private final GetOrderStatusUseCase getOrderStatus;
    private final CancelOrderUseCase cancelOrder;
    private final CurrentUserService currentUserService;
    private final Navigator navigator;

    @FXML private VBox ordersContainer;

    @FXML
    public void initialize() {
        loadCancellableOrders();
    }

    private void loadCancellableOrders() {
        BackgroundTasks.run(
                () -> getOrderStatus.execute(currentUserService.currentUser().id()),
                this::displayOrders,
                error -> showMessage("Error al cargar pedidos: " + error.getMessage())
        );
    }

    private void displayOrders(List<Order> orders) {
        ordersContainer.getChildren().clear();

        // Filtrar solo los pedidos en estado CREATED o CONFIRMED
        List<Order> cancellableOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.CONFIRMED) {
                cancellableOrders.add(order);
            }
        }

        if (cancellableOrders.isEmpty()) {
            showMessage("No tienes pedidos activos disponibles para cancelar.");
            return;
        }

        for (Order order : cancellableOrders) {
            ordersContainer.getChildren().add(buildOrderCard(order));
        }
    }

    private HBox buildOrderCard(Order order) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 1; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 14 18;");

        // Info izquierda: ID y total
        String shortId = "PED-" + order.getId().value().toString().substring(0, 8).toUpperCase();
        Label idLabel = new Label(shortId);
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        String totalText = order.getTotal() != null ? "$" + order.getTotal().value() : "$0.00";
        Label totalLabel = new Label("Total: " + totalText);
        totalLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");

        VBox infoBox = new VBox(4, idLabel, totalLabel);

        // Chip de estado
        Label statusChip = new Label(order.getStatus().name());
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            statusChip.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-size: 11px; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 8;");
        } else {
            statusChip.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #b45309; -fx-font-size: 11px; "
                    + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 3 8;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botón de cancelación
        Button cancelBtn = new Button("Cancelar pedido");
        cancelBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; "
                + "-fx-font-size: 12px; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-radius: 6; "
                + "-fx-padding: 6 14; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> promptCancellationReason(order));

        card.getChildren().addAll(infoBox, statusChip, spacer, cancelBtn);
        return card;
    }

    private void promptCancellationReason(Order order) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Pedido");
        dialog.setHeaderText("Cancelación del pedido: " + order.getId().value().toString().substring(0, 8));
        dialog.setContentText("Ingrese el motivo justificado de cancelación (obligatorio):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "El motivo de cancelación es obligatorio.");
                return;
            }

            CancelOrderCommand command = new CancelOrderCommand(
                    order.getId().value(),
                    currentUserService.currentUser().id(),
                    reason
            );

            BackgroundTasks.run(
                    () -> cancelOrder.execute(command),
                    success -> {
                        showAlert(Alert.AlertType.INFORMATION, "Pedido cancelado con éxito.");
                        loadCancellableOrders();
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "No se pudo cancelar el pedido: " + error.getMessage())
            );
        }
    }

    private void showMessage(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 20 0 0 0;");
        ordersContainer.getChildren().setAll(label);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestión de Pedidos");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void onBack() {
        navigator.show(OrderViews.MAIN);
    }
}