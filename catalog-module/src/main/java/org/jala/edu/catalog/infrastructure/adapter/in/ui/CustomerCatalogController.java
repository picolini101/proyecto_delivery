package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.usecase.ListAvailableProductsUseCase;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX de solo lectura para que el cliente consulte el catálogo de un comercio
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CustomerCatalogController {

    // TODO(sprint 2): reemplazar por el comercio elegido por el cliente cuando exista la lista de comercios.
    private static final UUID SELECTED_COMMERCE = SampleCatalog.COMMERCE;

    private static final String STYLESHEET = "customer-catalog.css";
    private static final double CARD_WIDTH = 200;
    private static final double IMAGE_HEIGHT = 140;

    static final String AVAILABLE = "Disponible";
    static final String SOLD_OUT = "Agotado";

    private final ListAvailableProductsUseCase listAvailableProducts;

    @FXML
    private FlowPane productsPane;

    @FXML
    private Button refreshButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        productsPane.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        reload();
    }

    @FXML
    void onRefresh() {
        reload();
    }

    static String availabilityLabel(boolean available) {
        return available ? AVAILABLE : SOLD_OUT;
    }

    private void reload() {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listAvailableProducts.execute(new ListAvailableProductsQuery(SELECTED_COMMERCE)),
                products -> {
                    showProducts(products);
                    showMessage(count(products.size()), "info");
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the catalog"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private void showProducts(List<CatalogProduct> products) {
        productsPane.getChildren().clear();

        if (products.isEmpty()) {
            productsPane.getChildren().add(new Label("This commerce has no products available."));
            return;
        }

        products.forEach(product -> productsPane.getChildren().add(buildCard(product)));
    }

    /**
     *  cuadro de imagen con etiqueta de disponibilidad y debajo nombre categoria y precio
     */
    private Node buildCard(CatalogProduct product) {
        Label initial = new Label(initialOf(product.name()));
        initial.getStyleClass().add("product-initial");

        Label badge = new Label(availabilityLabel(product.available()));
        badge.getStyleClass().addAll("badge", product.available() ? "badge-available" : "badge-sold-out");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));

        StackPane image = new StackPane(initial, badge);
        image.getStyleClass().add("product-image");
        image.setPrefSize(CARD_WIDTH, IMAGE_HEIGHT);
        image.setMinHeight(IMAGE_HEIGHT);

        Label name = new Label(product.name());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxWidth(CARD_WIDTH - 24);

        Label category = new Label(product.category());
        category.getStyleClass().add("product-category");

        Label price = new Label(product.price().toPlainString());
        price.getStyleClass().add("product-price");

        VBox details = new VBox(4, name, category, price);
        details.setPadding(new Insets(12));

        VBox card = new VBox(image, details);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        if (!product.available()) {
            card.getStyleClass().add("sold-out");
        }
        return card;
    }

    private static String initialOf(String name) {
        return name == null || name.isBlank() ? "?" : name.strip().substring(0, 1).toUpperCase();
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String count(int size) {
        return size == 1 ? "1 product" : size + " products";
    }
}