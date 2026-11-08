
package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.CatalogProductPage;
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
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Adaptador de entrada JavaFX de solo lectura para que el cliente consulte el catálogo de un comercio.
 * El cliente elige entre los comercios Activos; al habilitarse un comercio aparece en la lista.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CustomerCatalogController {

    private static final String STYLESHEET = "customer-catalog.css";
    private static final double MIN_CARD_WIDTH = 145;
    private static final double MAX_CARD_WIDTH = 240;
    private static final double MIN_IMAGE_HEIGHT = 85;
    private static final double MAX_IMAGE_HEIGHT = 150;
    private static final double CARD_GAP = 16;
    private static final int MAX_COLUMNS = 5;

    static final String AVAILABLE = "Disponible";
    static final String SOLD_OUT = "Agotado";

    private final ListAvailableProductsUseCase listAvailableProducts;
    private final ListCommercesUseCase listCommerces;

    private int currentPage = 1;
    private int totalPages;
    private int currentPageSize = 10;
    private List<CatalogProduct> currentProducts = List.of();

    private static final Duration SEARCH_DELAY = Duration.millis(300);
    private final PauseTransition searchDelay = new PauseTransition(SEARCH_DELAY);

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CommerceSummary> commerceSelector;

    @FXML
    private FlowPane productsPane;

    @FXML
    private ScrollPane catalogScrollPane;

    @FXML
    private Button refreshButton;

    @FXML
    private Button previousPageButton;

    @FXML
    private Button nextPageButton;

    @FXML
    private Label pageLabel;

    @FXML
    private HBox paginationControls;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        productsPane.getStylesheets().add(getClass().getResource(STYLESHEET).toExternalForm());

        productsPane.widthProperty().addListener((observable, previousWidth, newWidth) -> {
        if (newWidth.doubleValue() > 0 && !currentProducts.isEmpty()) {
            showProducts(currentProducts);
        }
    });
        commerceSelector.setConverter(new CommerceNameConverter());

        // Al elegir otro comercio se reinicia la paginación.
        commerceSelector.valueProperty().addListener((observable, previous, selected) -> {
            currentPage = 1;
            reload();
        });

        searchDelay.setOnFinished(event -> {
            currentPage = 1;
            reload();
        });
        searchField.textProperty().addListener(
                (observable, previous, text) -> searchDelay.playFromStart());

        updatePagination(0);
        loadActiveCommerces();
    }

    @FXML
    void onRefresh() {
        reload();
    }

    @FXML
    void onPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            reload();
        }
    }

    @FXML
    void onNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            reload();
        }
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
            updatePagination(0);
            return;
        }

        refreshButton.setDisable(true);
        previousPageButton.setDisable(true);
        nextPageButton.setDisable(true);

        int requestedPage = currentPage;
        String searchText = searchField.getText();

        BackgroundTasks.run(
                () -> listAvailableProducts.execute(
                        new ListAvailableProductsQuery(commerce.id(), requestedPage, searchText)),
                page -> {
                    if (!Objects.equals(searchText, searchField.getText())) {
                        return;
                    }
                    showProducts(page.products());

                    totalPages = page.totalPages();
                    updatePagination(totalPages);

                    if (page.products().isEmpty() && !searchText.isBlank()) {
                        showMessage("No products match your search.", "info");
                    } else if (page.totalAvailableProducts() == 0) {
                        showMessage("No products are currently available.", "info");
                    } else {
                        showMessage(count(page.totalProducts()), "info");
                    }

                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the catalog"), "error");
                    refreshButton.setDisable(false);
                    previousPageButton.setDisable(currentPage <= 1);
                    nextPageButton.setDisable(currentPage >= totalPages);
                });
    }

    private void updatePagination(int pages) {
        totalPages = pages;

        boolean showControls = pages > 1;
        paginationControls.setVisible(showControls);
        paginationControls.setManaged(showControls);

        pageLabel.setText("Page " + currentPage + " of " + pages);
        previousPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= pages);
    }

    private void showProducts(List<CatalogProduct> products) {
        currentProducts = List.copyOf(products);
        productsPane.getChildren().clear();

        if (currentProducts.isEmpty()) {
            return;
        }

        double availableWidth = Math.max(
                MIN_CARD_WIDTH,
                productsPane.getWidth() - 16);

        int columns = Math.min(
                MAX_COLUMNS,
                currentProducts.size());

        columns = Math.min(
                columns,
                Math.max(1, (int) ((availableWidth + CARD_GAP)
                        / (MIN_CARD_WIDTH + CARD_GAP))));

        double cardWidth = Math.min(
                MAX_CARD_WIDTH,
                (availableWidth - CARD_GAP * (columns - 1)) / columns);

        double imageHeight = Math.max(
                MIN_IMAGE_HEIGHT,
                Math.min(MAX_IMAGE_HEIGHT, cardWidth * 0.58));

        for (CatalogProduct product : currentProducts) {
            productsPane.getChildren().add(
                    buildCard(product, cardWidth, imageHeight));
        }
    }

    /**
     * Cuadro de imagen con etiqueta de disponibilidad y debajo nombre, categoría y precio.
     */
    private Node buildCard(
            CatalogProduct product,
            double cardWidth,
            double imageHeight) {

        Label initial = new Label(initialOf(product.name()));
        initial.getStyleClass().add("product-initial");

        Label badge = new Label(availabilityLabel(product.available()));
        badge.getStyleClass().addAll(
                "badge", product.available() ? "badge-available" : "badge-sold-out");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));

        StackPane image = new StackPane(initial, badge);
        image.getStyleClass().add("product-image");
        image.setPrefSize(cardWidth, imageHeight);
        image.setMinHeight(imageHeight);

        Label name = new Label(product.name());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxWidth(cardWidth - 16);

        Label category = new Label(product.category());
        category.getStyleClass().add("product-category");

        Label price = new Label(product.price().toPlainString());
        price.getStyleClass().add("product-price");

        VBox details = new VBox(4, name, category, price);
        details.setPadding(new Insets(8));

        VBox card = new VBox(image, details);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);

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
