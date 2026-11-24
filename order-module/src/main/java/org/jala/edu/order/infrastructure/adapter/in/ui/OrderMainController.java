package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.usecase.orders.ConsultarPedidosUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderDetailUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderTrackingUseCase;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderMainController {

    private final Navigator navigator;
    private final ConsultarPedidosUseCase consultarPedidosUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final GetOrderTrackingUseCase getOrderTrackingUseCase;
    private final CurrentUserService currentUserService;
    private final SpringFxmlLoader fxmlLoader;

    @FXML private VBox ordersContainer;
    @FXML private Label emptyLabel;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    public void initialize() {
        loadOrders();
    }

    @FXML
    void onNewCart() {
        navigator.show(OrderViews.NEW_CART);
    }

    private void loadOrders() {
        BackgroundTasks.run(
                () -> consultarPedidosUseCase.execute(currentUserService.currentUser().id()),
                orders -> {
                    ordersContainer.getChildren().clear();
                    if (orders.isEmpty()) {
                        emptyLabel.setVisible(true);
                        ordersContainer.getChildren().add(emptyLabel);
                    } else {
                        emptyLabel.setVisible(false);
                        List<OrderSummary> summaries = orders.stream().map(OrderSummary::from).toList();
                        for (OrderSummary summary : summaries) {
                            ordersContainer.getChildren().add(createOrderCard(summary));
                        }
                    }
                },
                error -> {
                    emptyLabel.setText("Error al cargar pedidos");
                    emptyLabel.setVisible(true);
                }
        );
    }

    private HBox createOrderCard(OrderSummary summary) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); " +
                "-fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

        VBox infoBox = new VBox(5);
        Label idLabel = new Label("Pedido #" + summary.shortId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Label dateLabel = new Label(dateFormatter.format(summary.createdAt()));
        dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(idLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox actionBox = new VBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Label totalLabel = new Label("$" + summary.subtotal());
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0f172a;");

        Label statusLabel = new Label(summary.status());
        statusLabel.setStyle(getStatusStyle(summary.status()));

        // Btn detalle
        Button btnDetail = new Button("Ver Detalle");
        btnDetail.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        btnDetail.setOnAction(e -> showDetailModal(summary.orderId()));

        // Btn estado
        Button btnStatus = new Button("Ver Estado");
        btnStatus.setStyle("-fx-background-color: #10b981; -fx-text-fill: white" + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        btnStatus.setOnAction(e -> showTrackingModal(summary.orderId()));

        HBox statusAndButtons = new HBox(10, statusLabel, btnDetail, btnStatus);
        statusAndButtons.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().addAll(totalLabel, statusAndButtons);

        card.getChildren().addAll(infoBox, spacer, actionBox);
        return card;
    }

    private String getStatusStyle(String status) {
        String baseStyle = "-fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; ";
        return switch (status) {
            case "CONFIRMED" -> baseStyle + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "CANCELLED" -> baseStyle + "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            case "DELIVERED" -> baseStyle + "-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3;";
            default -> baseStyle + "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        };
    }


    private void showDetailModal(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderDetailUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                order -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID del Pedido: ").append(order.getId().value()).append("\n");
                    String fullId = order.getId().value().toString();
                    sb.append("Código de Entrega: ").append(fullId.substring(fullId.length() - 4).toUpperCase()).append("\n\n");

                    sb.append("Dirección: ").append(order.getDeliveryAddress() != null ? order.getDeliveryAddress().toString() : "No registrada").append("\n\n");
                    sb.append("Productos:\n");
                    order.getLines().forEach(line ->
                            sb.append("- ").append(line.getQuantity().value()).append("x ")
                                    .append(line.getProductName()).append(" ($").append(line.getSubtotal().value()).append(")\n")
                    );

                    java.math.BigDecimal subtotalCalculado = order.getLines().stream()
                            .map(l -> l.getSubtotal().value())
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                    java.math.BigDecimal envioReal = new java.math.BigDecimal("10.00");
                    java.math.BigDecimal totalReal = subtotalCalculado.add(envioReal);

                    sb.append(String.format("\nCosto de Envío: $%.2f", envioReal));
                    sb.append(String.format("\nTOTAL: $%.2f", totalReal));

                    Dialogs.confirm(ordersContainer, "Detalle del Pedido", sb.toString());
                },
                error -> showErrorAlert("Error al cargar detalle", error.getMessage())
        );
    }


    private void showTrackingModal(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderTrackingUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                summary -> {
                    SpringFxmlLoader.View<OrderTrackingController> view = fxmlLoader.loadView(OrderViews.TRACKING);
                    view.controller().prepare(summary);
                    Dialogs.showModal(ordersContainer, "Estado del Pedido", view.root());
                },
                error -> showErrorAlert("Error al cargar estado", error.getMessage())
        );
    }

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}