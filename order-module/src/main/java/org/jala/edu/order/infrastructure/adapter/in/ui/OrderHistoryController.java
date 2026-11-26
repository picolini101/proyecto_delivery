package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.port.in.CancelOrderCommand;
import org.jala.edu.order.application.usecase.orders.CancelOrderUseCase;
import org.jala.edu.order.application.usecase.orders.ConsultarPedidosUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderDetailUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderTrackingUseCase;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderHistoryController {

    private final ConsultarPedidosUseCase consultarPedidosUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final CurrentUserService currentUserService;
    private final GetOrderTrackingUseCase getOrderTrackingUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final SpringFxmlLoader fxmlLoader;

    @FXML private TableView<OrderSummary> ordersTable;
    @FXML private TableColumn<OrderSummary, String> colId;
    @FXML private TableColumn<OrderSummary, String> colDate;
    @FXML private TableColumn<OrderSummary, String> colTotal;
    @FXML private TableColumn<OrderSummary, String> colStatus;
    @FXML private TableColumn<OrderSummary, Void> colAction;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

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

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDetail = new Button("Ver Detalle");
            private final Button btnStatus = new Button("Estado");
            private final Button btnCancel = new Button("Cancelar");

            {
                btnDetail.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

                btnStatus.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

                btnCancel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; "
                        + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

                btnDetail.setOnAction(event -> {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    showDetailModal(summary.orderId());
                });

                btnStatus.setOnAction(event -> {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    showTrackingScreen(summary.orderId());
                });

                btnCancel.setOnAction(event -> {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    promptCancellationReason(summary);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    OrderSummary summary = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(6, btnDetail, btnStatus);
                    box.setAlignment(Pos.CENTER);

                    // Solo disponible para pedidos en estado CREATED o CONFIRMED
                    boolean canCancel = "CREATED".equalsIgnoreCase(summary.status())
                            || "CONFIRMED".equalsIgnoreCase(summary.status());
                    if (canCancel) {
                        box.getChildren().add(btnCancel);
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void promptCancellationReason(OrderSummary summary) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Pedido");
        dialog.setHeaderText("Cancelación del pedido #" + summary.shortId());
        dialog.setContentText("Ingrese el motivo justificado de cancelación (obligatorio):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                showErrorAlert("Validación", "El motivo de cancelación es obligatorio.");
                return;
            }

            CancelOrderCommand command = new CancelOrderCommand(
                    summary.orderId(),
                    currentUserService.currentUser().id(),
                    reason
            );

            BackgroundTasks.run(
                    () -> cancelOrderUseCase.execute(command),
                    success -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Gestión de Pedidos");
                        alert.setHeaderText(null);
                        alert.setContentText("Pedido cancelado con éxito.");
                        alert.showAndWait();
                        loadOrders();
                    },
                    error -> showErrorAlert("No se pudo cancelar el pedido", error.getMessage())
            );
        }
    }

    private void showTrackingScreen(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderTrackingUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                summary -> {
                    SpringFxmlLoader.View<OrderTrackingController> view = fxmlLoader.loadView(OrderViews.TRACKING);
                    view.controller().prepare(summary);
                    Dialogs.showModal(ordersTable, "Estado del Pedido", view.root());
                },
                error -> showErrorAlert("Error al cargar estado", error.getMessage())
        );
    }

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

    private void showDetailModal(java.util.UUID orderId) {
        BackgroundTasks.run(
                () -> getOrderDetailUseCase.execute(currentUserService.currentUser().id(), OrderId.of(orderId)),
                order -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID del Pedido: ").append(order.getId().value()).append("\n");
                    String fullId = order.getId().value().toString();
                    String deliveryCode = fullId.substring(fullId.length() - 4).toUpperCase();
                    sb.append("Código de Entrega: ").append(deliveryCode).append("\n\n");
                    sb.append("Dirección: ").append(order.getDeliveryAddress() != null
                            ? order.getDeliveryAddress().toString() : "No registrada").append("\n\n");
                    sb.append("Productos:\n");
                    order.getLines().forEach(line ->
                            sb.append("- ").append(line.getQuantity().value()).append("x ")
                                    .append(line.getProductName()).append(" ($").append(line.getSubtotal().value()).append(")\n")
                    );

                    java.math.BigDecimal envioReal = order.getShippingCost() != null
                            ? order.getShippingCost().value() : java.math.BigDecimal.ZERO;
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

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}