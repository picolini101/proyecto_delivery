package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.order.application.usecase.orders.ListFailedPaymentsUseCase;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Pantalla principal de pedidos. Muestra una carta horizontal por cada pedido cancelado por pago rechazado.
 * Es PROTOTYPE: cada vez que se abre la pantalla se crea uno nuevo y las cartas se cargan de nuevo.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderMainController {

    private static final String CARD_STYLE = "-fx-background-color: white; -fx-background-radius: 8; "
            + "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 #dc2626; -fx-border-width: 1 1 1 5; "
            + "-fx-border-radius: 8; -fx-padding: 14 18;";

    private final Navigator navigator;
    private final CurrentUserService currentUserService;
    private final ListFailedPaymentsUseCase listFailedPayments;
    private final FailedOrderCardPresenter presenter = new FailedOrderCardPresenter();

    @FXML private VBox cardsBox;

    @FXML
    void initialize() {
        BackgroundTasks.run(
                () -> listFailedPayments.execute(currentUserService.currentUser().id()),
                items -> show(items.stream().map(presenter::present).toList()),
                error -> showMessage("No se pudieron cargar tus pedidos"));
    }

    private void show(List<FailedOrderCard> cards) {
        cardsBox.getChildren().clear();
        if (cards.isEmpty()) {
            showMessage("No tienes pedidos todavía");
            return;
        }
        cards.forEach(card -> cardsBox.getChildren().add(buildCard(card)));
    }

    private void showMessage(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
        cardsBox.getChildren().setAll(label);
    }

    /** Carta rectangular ancha: titulo y chips a la izquierda, fecha a la derecha. */
    private HBox buildCard(FailedOrderCard card) {
        Label title = new Label(card.title());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox chips = new HBox(8, chip(card.status(), "#fee2e2", "#b91c1c"), chip(card.reason(), "#e2e8f0", "#334155"));
        VBox info = new VBox(6, title, chips);
        if (!card.detail().isBlank()) {
            info.getChildren().add(text(card.detail(), "#475569"));
        }
        info.getChildren().add(text(card.hint(), "#16a34a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox box = new HBox(12, info, spacer, text(card.date(), "#64748b"));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(CARD_STYLE);
        return box;
    }

    private Label chip(String text, String background, String foreground) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + background + "; -fx-text-fill: " + foreground + "; "
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 2 10;");
        return label;
    }

    private Label text(String value, String color) {
        Label label = new Label(value);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
        return label;
    }

    @FXML
    void onNewCart() {
        navigator.show(OrderViews.NEW_CART);
    }

    @FXML
    void onViewHistory() {
        navigator.show(OrderViews.HISTORY);
    }

}
