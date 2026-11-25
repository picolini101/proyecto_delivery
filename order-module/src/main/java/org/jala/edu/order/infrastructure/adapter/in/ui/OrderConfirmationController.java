package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.order.application.dto.OrderConfirmationReceipt;
import org.jala.edu.order.application.usecase.orders.GetOrderConfirmationUseCase;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderConfirmationController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final GetOrderConfirmationUseCase getOrderConfirmation;
    private final CurrentUserService currentUserService;
    private final OrderConfirmationContext orderContext;
    private final Navigator navigator;

    @FXML private Label deliveryCodeLabel;
    @FXML private Label invoiceNumberLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label addressLabel;
    @FXML private Label statusLabel;
    @FXML private VBox productsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label shippingLabel;
    @FXML private Label totalLabel;

    @FXML
    public void initialize() {
        if (orderContext == null || currentUserService == null || currentUserService.currentUser() == null) {
            return;
        }
        UUID orderId = orderContext.getLastConfirmedOrderId();
        UUID customerId = currentUserService.currentUser().id();

        if (orderId != null && customerId != null) {
            try {
                OrderConfirmationReceipt receipt = getOrderConfirmation.execute(orderId, customerId);
                displayReceipt(receipt);
            } catch (Exception ex) {
                invoiceNumberLabel.setText("Error al cargar comprobante");
            }
        }
    }

    public void displayReceipt(OrderConfirmationReceipt receipt) {
        deliveryCodeLabel.setText(receipt.deliveryCode());
        invoiceNumberLabel.setText("FAC-" + receipt.orderId().toString().substring(0, 8).toUpperCase());

        if (receipt.confirmedAt() != null) {
            orderDateLabel.setText(DATE_FORMATTER.format(receipt.confirmedAt()));
        } else {
            orderDateLabel.setText("Fecha no registrada");
        }

        customerNameLabel.setText(currentUserService.currentUser().displayName());
        addressLabel.setText(receipt.deliveryAddress());
        statusLabel.setText("COMPRA CONFIRMADA (" + receipt.status() + ")");

        productsContainer.getChildren().clear();
        for (OrderConfirmationReceipt.LineReceipt line : receipt.lines()) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 10 14; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6;");

            Label nameQty = new Label(line.quantity() + "x  " + line.productName());
            nameQty.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 13px; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label unitPrice = new Label("Precio unitario: $" + line.unitPrice());
            unitPrice.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-padding: 0 16 0 0;");

            Label lineSubtotal = new Label("$" + line.subtotal());
            lineSubtotal.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-font-weight: bold;");

            row.getChildren().addAll(nameQty, spacer, unitPrice, lineSubtotal);
            productsContainer.getChildren().add(row);
        }

        subtotalLabel.setText("$" + receipt.subtotal());
        shippingLabel.setText("$" + receipt.shippingCost());
        totalLabel.setText("$" + receipt.total());
    }

    @FXML
    void onFinish() {
        navigator.show(OrderViews.MAIN);
    }
}
