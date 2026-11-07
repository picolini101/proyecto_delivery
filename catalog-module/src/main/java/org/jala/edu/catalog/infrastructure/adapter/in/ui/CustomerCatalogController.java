package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.application.usecase.ListAvailableProductsUseCase;
import org.jala.edu.catalog.application.usecase.ListCommercesUseCase;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX de solo lectura para que el cliente consulte el catálogo de un comercio.
 * El cliente elige entre los comercios Activos; al habilitarse un comercio aparece en la lista.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CustomerCatalogController {

    private static final String STYLESHEET = "customer-catalog.css";
    private static final double CARD_WIDTH = 200;
    private static final double IMAGE_HEIGHT = 140;

    static final String AVAILABLE = "Disponible";
    static final String SOLD_OUT = "Agotado";

    private final ListAvailableProductsUseCase listAvailableProducts;
    private final ListCommercesUseCase listCommerces;

    @FXML
    private ComboBox<CommerceSummary> commerceSelector;

    @FXML
    private FlowPane productsPane;

    @FXML
    private Button refreshButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        productsPane.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());
        commerceSelector.setConverter(new CommerceNameConverter());
        // Al elegir otro comercio se recargan sus productos
        commerceSelector.valueProperty().addListener((observable, previous, selected) -> reload());
        loadActiveCommerces();
    }

    @FXML
    void onRefresh() {
        reload();
    }

    static String availabilityLabel(boolean available) {
        return available ? AVAILABLE : SOLD_OUT;
    }

    /** El cliente solo ve comercios Activos; se selecciona el primero para mostrar su catálogo. */
    private void loadActiveCommerces() {
        BackgroundTasks.run(
                () -> listCommerces.execute(ListCommercesQuery.active()),
                commerces -> {
                    commerceSelector.getItems().setAll(commerces);
                    if (commerces.isEmpty()) {
                        showMessage("There are no commerces available.", "info");
                    } else {
                        commerceSelector.setValue(commerces.get(0));
                    }
                },
                error -> showMessage(ErrorMessages.of(error, "load the commerces"), "error"));
    }

    private void reload() {
        CommerceSummary commerce = commerceSelector.getValue();
        if (commerce == null) {
            productsPane.getChildren().clear();
            return;
        }

        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listAvailableProducts.execute(new ListAvailableProductsQuery(commerce.id())),
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
     * Cuadro de imagen con etiqueta de disponibilidad y debajo nombre, categoría y precio.
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
        MessageLabels.show(messageLabel, text, styleClass);
    }

    private static String count(int size) {
        return size == 1 ? "1 product" : size + " products";
    }
}