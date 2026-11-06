package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.math.BigDecimal;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.usecase.CartSummary;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.RemoveProductFromCartUseCase;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CartItem;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class RemoveItemPanelController {
    private static final UUID CURRENT_CUSTOMER = SampleUsers.CUSTOMER;
    private final RemoveProductFromCartUseCase removeProductFromCart;
    private final GetCartSummaryUseCase getCartSummary;
    private final CartRepositoryPort cartRepository;
    private final Navigator navigator;

    @FXML private VBox itemsContainer;
    @FXML private Label statusLabel;
    @FXML private Label emptyCartLabel;
    @FXML private Label subtotalValueLabel;
    @FXML private Label shippingValueLabel;
    @FXML private Label totalValueLabel;
    @FXML private Button refreshButton;


    @FXML
    void initialize() {
        refreshView();
    }
    @FXML
    void onRefresh() {
        refreshView();
    }

    @FXML
    void onBackToShop() {
        navigator.show(OrderViews.NEW_CART);
    }

    public void refreshView() {
        BackgroundTasks.run(
                () -> {
                    Cart cart = cartRepository.findOrCreate(CURRENT_CUSTOMER);
                    CartSummary summary = getCartSummary.execute(CURRENT_CUSTOMER);
                    return new CartViewModel(cart, summary);
                },
                model -> renderCart(model.cart(), model.summary()),
                error -> showFeedback("Error al cargar carrito: " + ErrorMessages.of(error, "consultar carrito"), false)
        );
    }

    private void renderCart(Cart cart, CartSummary summary) {
        itemsContainer.getChildren().clear();
        boolean isEmpty = cart.getItems().isEmpty();
        emptyCartLabel.setVisible(isEmpty);
        emptyCartLabel.setManaged(isEmpty);
        if (!isEmpty) {
            for (CartItem item : cart.getItems()) {
                itemsContainer.getChildren().add(buildItemRow(item.getProductId(), item.getQuantity()));
            }
        }
        subtotalValueLabel.setText("$. " + summary.subtotal().setScale(2));
        shippingValueLabel.setText("$. " + summary.shipping().setScale(2));
        totalValueLabel.setText("$. " + summary.total().setScale(2));
    }

    private HBox buildItemRow(UUID productId, int quantity) {
        Label nameLabel = new Label("Producto ID: " + shortId(productId));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label qtyLabel = new Label("Cant: " + quantity);
        qtyLabel.setStyle("-fx-text-fill: #64748b; -fx-padding: 0 0 0 10;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button deleteBtn = new Button("Eliminar");
        deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
        // Confirmacion obligatoria mediante el contrato de Dialogs de core-module
        deleteBtn.setOnAction(event -> {
            boolean confirmed = Dialogs.confirm(
                    deleteBtn,
                    "Confirmar Eliminación",
                    "¿Está seguro de que desea eliminar este producto del carrito?"
            );
            if (confirmed) {
                executeRemoval(productId, deleteBtn);
            }
        });
        HBox row = new HBox(10, nameLabel, qtyLabel, spacer, deleteBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-padding: 10 14; -fx-background-radius: 6; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        return row;
    }
    private void executeRemoval(UUID productId, Button triggerButton) {
        triggerButton.setDisable(true);
        BackgroundTasks.run(
                () -> removeProductFromCart.execute(CURRENT_CUSTOMER, productId),
                updatedCart -> {
                    showFeedback("Producto eliminado exitosamente del carrito.", true);
                    refreshView();
                },
                error -> {
                    triggerButton.setDisable(false);
                    showFeedback("No se pudo eliminar: " + ErrorMessages.of(error, "eliminar producto"), false);
                }
        );
    }
    private void showFeedback(String message, boolean success) {
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        statusLabel.setText(message);
        if (success) {
            statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 8 12; -fx-background-radius: 6;");
        } else {
            statusLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 8 12; -fx-background-radius: 6;");
        }
    }
    private static String shortId(UUID id) {
        return id.toString().substring(0, 8) + "...";
    }
    private record CartViewModel(Cart cart, CartSummary summary) {}
}