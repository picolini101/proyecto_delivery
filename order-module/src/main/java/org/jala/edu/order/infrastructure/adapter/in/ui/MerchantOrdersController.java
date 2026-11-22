package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.order.application.port.in.MarkOrderReadyForDeliveryCommand;
import org.jala.edu.order.application.usecase.orders.ListConfirmedOrdersByMerchantUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderReadyForDeliveryUseCase;
import org.jala.edu.order.domain.model.Order;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MerchantOrdersController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final CurrentUserService currentUserService;
    private final ListConfirmedOrdersByMerchantUseCase listOrders;
    private final MarkOrderReadyForDeliveryUseCase markReady;

    @FXML private VBox ordersBox;

    public MerchantOrdersController(CurrentUserService currentUserService,
                                    ListConfirmedOrdersByMerchantUseCase listOrders,
                                    MarkOrderReadyForDeliveryUseCase markReady) {
        this.currentUserService = currentUserService;
        this.listOrders = listOrders;
        this.markReady = markReady;
    }

    @FXML
    void initialize() {
        loadOrders();
    }

    private void loadOrders() {
        BackgroundTasks.run(
                () -> listOrders.execute(currentUserService.currentUser().id()),
                this::showOrders,
                error -> showMessage("No se pudieron cargar los pedidos"));
    }

    private void showOrders(List<Order> orders) {
        ordersBox.getChildren().clear();
        if (orders.isEmpty()) {
            showMessage("No tienes pedidos confirmados");
            return;
        }
        orders.forEach(order -> ordersBox.getChildren().add(orderCard(order)));
    }

    private HBox orderCard(Order order) {
        Label number = new Label("Pedido #" + order.getId().value().toString().substring(0, 8).toUpperCase());
        number.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        Label details = new Label("Confirmado · " + order.getLines().stream()
                .mapToInt(line -> line.getQuantity().value()).sum() + " artículos · "
                + order.getSubtotal().value() + " · " + DATE_FORMAT.format(order.getCreatedAt()));
        details.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        VBox info = new VBox(6, number, details);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button readyButton = new Button("Listo para entrega");
        readyButton.setOnAction(event -> markReady(order, readyButton));
        HBox card = new HBox(14, info, spacer, readyButton);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 14 18;");
        return card;
    }

    private void markReady(Order order, Button button) {
        button.setDisable(true);
        BackgroundTasks.run(() -> {
            UUID merchantId = currentUserService.currentUser().id();
            markReady.execute(new MarkOrderReadyForDeliveryCommand(order.getId().value(), merchantId, merchantId));
            return null;
        }, ignored -> loadOrders(), error -> {
            button.setDisable(false);
            showMessage("No se pudo marcar el pedido como listo para entrega");
        });
    }

    private void showMessage(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 12 0;");
        ordersBox.getChildren().setAll(label);
    }
}
