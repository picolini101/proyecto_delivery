package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.dto.DeleteProductResult;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.port.in.UpdateProductStatusCommand;
import org.jala.edu.catalog.application.usecase.DeleteProductUseCase;
import org.jala.edu.catalog.application.usecase.ListManagedProductsUseCase;
import org.jala.edu.catalog.application.usecase.UpdateProductStatusUseCase;
import org.jala.edu.catalog.domain.model.ProductStatus;
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

    private final BooleanProperty updatingAvailability = new SimpleBooleanProperty(false);

    // Evita pulsar Eliminar dos veces mientras la eliminación está en curso
    private final BooleanProperty deleting = new SimpleBooleanProperty(false);

    private final ListManagedProductsUseCase listManagedProductsUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
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
    private TableColumn<ManagedProduct, String> commerceColumn;

    @FXML
    private TableColumn<ManagedProduct, String> availabilityColumn;

    @FXML
    private TableColumn<ManagedProduct, String> createdAtColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private ComboBox<ProductStatus> availabilitySelector;

    @FXML
    private Button updateAvailabilityButton;

    @FXML
    private Label messageLabel;

    @FXML
    void initialize() {
        bind(nameColumn, managed -> managed.product().getName());
        bind(commerceColumn, ManagedProduct::commerceName);
        bind(categoryColumn, managed -> managed.product().getCategory());
        bind(priceColumn, managed -> managed.product().getPrice().toPlainString());
        bind(stockColumn, managed -> String.valueOf(managed.product().getStock()));
        bind(availabilityColumn, ManagedProduct::availability);
        bind(createdAtColumn, managed -> DisplayFormat.dateTime(managed.product().getCreatedAt()));

        // El botón Editar solo se habilita cuando hay un producto seleccionado
        editButton.disableProperty().bind(productsTable.getSelectionModel().selectedItemProperty().isNull());

        // Eliminar requiere un producto seleccionado y que no haya otra eliminación en curso
        deleteButton.disableProperty().bind(
                productsTable.getSelectionModel().selectedItemProperty().isNull().or(deleting));

        // El selector solo permite los estados Disponible y Agotado
        availabilitySelector.setItems(FXCollections.observableArrayList(
                ProductStatus.AVAILABLE,
                ProductStatus.OUT_OF_STOCK));

        availabilitySelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProductStatus status) {
                if (status == null) {
                    return "";
                }
                return status == ProductStatus.AVAILABLE ? "Disponible" : "Agotado";
            }

            @Override
            public ProductStatus fromString(String value) {
                return null;
            }
        });

        // El botón de disponibilidad requiere un producto y un estado seleccionado
        updateAvailabilityButton.disableProperty().bind(
                productsTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(availabilitySelector.valueProperty().isNull())
                        .or(updatingAvailability));

        // Al seleccionar un producto, muestra su estado actual
        productsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected == null) {
                        availabilitySelector.setValue(null);
                    } else {
                        ProductStatus status = selected.product().getStatus();
                        availabilitySelector.setValue(
                                status == ProductStatus.AVAILABLE
                                        || status == ProductStatus.OUT_OF_STOCK
                                        ? status
                                        : null);
                    }
                });

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

        if (selected == null) {
            return;
        }

        SpringFxmlLoader.View<EditProductController> view = fxmlLoader.loadView(CatalogViews.EDIT_PRODUCT);
        view.controller().edit(selected.product(), () -> reload("Product " + selected.product().getName() + " updated."));
        Dialogs.showModal(productsTable, "Edit product", view.root());
    }

    /**
     * Actualiza el estado de disponibilidad del producto seleccionado sin modificar su stock.
     */
    @FXML
    void onUpdateAvailability() {
        ManagedProduct selected = productsTable.getSelectionModel().getSelectedItem();
        ProductStatus newStatus = availabilitySelector.getValue();

        if (selected == null || newStatus == null) {
            return;
        }

        UpdateProductStatusCommand command = new UpdateProductStatusCommand(
                CatalogSession.CURRENT_USER,
                selected.product().getId().value(),
                newStatus);

        updatingAvailability.set(true);
        availabilitySelector.setDisable(true);

        BackgroundTasks.run(
                () -> updateProductStatusUseCase.execute(command),
                done -> {
                    updatingAvailability.set(false);
                    availabilitySelector.setDisable(false);
                    reload("Availability updated successfully.");
                },
                error -> {
                    updatingAvailability.set(false);
                    availabilitySelector.setDisable(false);
                    showMessage(ErrorMessages.of(error, "update availability"), "error");
                });
    }

    /**
     * Elimina el producto seleccionado tras pedir confirmación. Si el administrador cancela,
     * no se ejecuta nada y el producto no se modifica.
     */
    @FXML
    void onDelete() {
        ManagedProduct selected = productsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            return;
        }

        boolean confirmed = Dialogs.confirm(
                productsTable,
                "¿Eliminar el producto " + selected.product().getName() + "?",
                "También se eliminará su stock. Esta acción no se puede deshacer.");

        if (!confirmed) {
            return;
        }

        DeleteProductCommand command = new DeleteProductCommand(
                CatalogSession.CURRENT_USER,
                selected.product().getId().value());

        deleting.set(true);

        BackgroundTasks.run(
                () -> deleteProductUseCase.execute(command),
                result -> {
                    deleting.set(false);
                    reload(deleteMessage(result));
                },
                error -> {
                    deleting.set(false);
                    showMessage(ErrorMessages.of(error, "eliminar el producto"), "error");
                });
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

    // Mensaje de éxito; avisa si el comercio quedó Deshabilitado
    private static String deleteMessage(DeleteProductResult result) {
        return result == DeleteProductResult.DELETED_COMMERCE_DISABLED
                ? "Producto eliminado. El comercio quedó Deshabilitado porque ya no tiene productos disponibles."
                : "Producto eliminado correctamente.";
    }

    private static String count(int size) {
        return size == 1 ? "1 product" : size + " products";
    }

    private static void bind(TableColumn<ManagedProduct, String> column, Function<ManagedProduct, String> value) {
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
    }
}