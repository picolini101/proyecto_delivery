package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.usecase.ListManagedProductsUseCase;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada JavaFX para listar los productos que el administrador o comerciante gestiona
 * (vista: {@code manage-products.fxml}). Es también el punto de entrada para editar el producto
 * seleccionado, que se abre en un diálogo.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ManageProductsController {

    private final ListManagedProductsUseCase listManagedProductsUseCase;
    private final SpringFxmlLoader fxmlLoader;

    @FXML
    private TableView<ManagedProduct> productsTable;

    @FXML
    private TableColumn<ManagedProduct, String> nameColumn;

    @FXML
    private TableColumn<ManagedProduct, String> categoryColumn;

    @FXML
    private TableColumn<ManagedProduct, String> priceColumn;

    @FXML
    private TableColumn<ManagedProduct, String> stockColumn;

    @FXML
    private TableColumn<ManagedProduct, String> availabilityColumn;

    @FXML
    private TableColumn<ManagedProduct, String> createdAtColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button editButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        bind(nameColumn, managed -> managed.product().getName());
        bind(categoryColumn, managed -> managed.product().getCategory());
        bind(priceColumn, managed -> managed.product().getPrice().toPlainString());
        bind(stockColumn, managed -> String.valueOf(managed.product().getStock()));
        bind(availabilityColumn, ManagedProduct::availability);
        bind(createdAtColumn, managed -> DisplayFormat.dateTime(managed.product().getCreatedAt()));

        // El botón Editar solo se habilita cuando hay un producto seleccionado
        editButton.disableProperty().bind(productsTable.getSelectionModel().selectedItemProperty().isNull());

        // Doble clic sobre una fila equivale a pulsar Editar
        productsTable.setRowFactory(table -> {
            TableRow<ManagedProduct> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        reload(null);
    }

    @FXML
    void onRefresh() {
        reload(null);
    }

    @FXML
    void onEdit() {
        ManagedProduct selected = productsTable.getSelectionModel().getSelectedItem();
        SpringFxmlLoader.View<EditProductController> view = fxmlLoader.loadView(CatalogViews.EDIT_PRODUCT);
        view.controller().edit(selected.product(), () -> reload("Product " + selected.product().getName() + " updated."));
        Dialogs.showModal(productsTable, "Edit product", view.root());
    }

    /**
     * Carga los productos; luego muestra {@code successMessage}, o el conteo si es {@code null}.
     */
    private void reload(String successMessage) {
        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> listManagedProductsUseCase.execute(new ListManagedProductsQuery()),
                products -> {
                    productsTable.getItems().setAll(products);
                    if (successMessage == null) {
                        showMessage(count(products.size()), "info");
                    } else {
                        showMessage(successMessage, "success");
                    }
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the products"), "error");
                    refreshButton.setDisable(false);
                });
    }

    private void showMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    private static String count(int size) {
        return size == 1 ? "1 product" : size + " products";
    }

    private static void bind(TableColumn<ManagedProduct, String> column, Function<ManagedProduct, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}