package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
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
public class OrderHistoryController {

    private final ConsultarPedidosUseCase consultarPedidosUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final CurrentUserService currentUserService;
    private final GetOrderTrackingUseCase getOrderTrackingUseCase;
    private final SpringFxmlLoader fxmlLoader;

    @FXML private TableView<OrderSummary> ordersTable;
    @FXML private TableColumn<OrderSummary, String> colId;
    @FXML private TableColumn<OrderSummary, String> colDate;
    @FXML private TableColumn<OrderSummary, String> colTotal;
    @FXML private TableColumn<OrderSummary, String> colStatus;
    @FXML private TableColumn<OrderSummary, Void> colAction;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    @FXML
    void initialize() {
        setupTableColumns();
        loadOrders();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().shortId()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(dateFormatter.format(data.getValue().createdAt())));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().subtotal()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status()));

        // Botón "Ver Detalle" en cada fila
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetail = new Button("Ver Detalle");
            private final Button btnStatus = new Button("Estado");
            {
                btnDetail.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
                btnStatus.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");

                btnDetail.setOnAction(event -> {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    showDetailModal(summary.orderId());
                });

                btnStatus.setOnAction(event -> {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    showTrackingScreen(summary.orderId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, btnDetail, btnStatus);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void showTrackingScreen(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderTrackingUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                summary -> {
                    // Cargamos el FXML de las bolitas (Stepper)
                    SpringFxmlLoader.View<OrderTrackingController> view = fxmlLoader.loadView(OrderViews.TRACKING);
                    // Le pasamos los datos
                    view.controller().prepare(summary);
                    // Lo mostramos como modal
                    Dialogs.showModal(ordersTable, "Estado del Pedido", view.root());
                },
                error -> showErrorAlert("Error al cargar estado", error.getMessage())
        );
    }

    /**
     * Carga la lista de pedidos en la tabla.
     */
    private void loadOrders() {
        BackgroundTasks.run(
                () -> consultarPedidosUseCase.execute(currentUserService.currentUser().id()),
                orders -> {
                    List<OrderSummary> summaries = orders.stream().map(OrderSummary::from).toList();
                    ordersTable.setItems(FXCollections.observableArrayList(summaries));
                },
                error -> showErrorAlert("Error al cargar historial", error.getMessage())
        );
    }

    /**
     * Muestra el desglose completo del pedido .
     */
    private void showDetailModal(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderDetailUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                order -> {

                    StringBuilder sb = new StringBuilder();
                    sb.append("ID del Pedido: ").append(order.getId().value()).append("\n");

                    String fullId = order.getId().value().toString();
                    String deliveryCode = fullId.substring(fullId.length() - 4).toUpperCase();
                    sb.append("Código de Entrega: ").append(deliveryCode).append("\n\n");

                    sb.append("Dirección: ").append(order.getDeliveryAddress() != null ? order.getDeliveryAddress().toString() : "No registrada").append("\n\n");
                    sb.append("Productos:\n");

                    order.getLines().forEach(line ->
                            sb.append("- ").append(line.getQuantity().value()).append("x ")
                                    .append(line.getProductName()).append(" ($").append(line.getSubtotal().value()).append(")\n")
                    );

                    java.math.BigDecimal envioReal = order.getShippingCost() != null ? order.getShippingCost().value() : java.math.BigDecimal.ZERO;
                    if (envioReal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        envioReal = new java.math.BigDecimal("10.00");
                    }

                    java.math.BigDecimal totalReal = order.getTotal() != null ? order.getTotal().value() : java.math.BigDecimal.ZERO;
                    if (totalReal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        totalReal = order.getSubtotal().value().add(envioReal);
                    }


                    sb.append(String.format("\nCosto de Envío: $%.2f", envioReal));
                    sb.append(String.format("\nTOTAL: $%.2f", totalReal));

                    Dialogs.confirm(ordersTable, "Detalle del Pedido", sb.toString());
                },
                error -> showErrorAlert("Error al cargar detalle", error.getMessage())
        );
    }

    /**
     * Helper para mostrar errores sin depender de Dialogs.showError.
     */
    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}