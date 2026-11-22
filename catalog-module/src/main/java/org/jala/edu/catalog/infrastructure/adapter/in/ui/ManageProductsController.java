package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.UUID;
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
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.dto.DeleteProductResult;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.port.in.UpdateProductStatusCommand;
import org.jala.edu.catalog.application.port.in.DisableProductCommand;
import org.jala.edu.catalog.application.port.in.EnableProductCommand;
import org.jala.edu.catalog.application.usecase.DeleteProductUseCase;
import org.jala.edu.catalog.application.usecase.ListCommercesUseCase;
import org.jala.edu.catalog.application.usecase.ListManagedProductsUseCase;
import org.jala.edu.catalog.application.usecase.UpdateProductStatusUseCase;
import org.jala.edu.catalog.application.usecase.DisableProductUseCase;
import org.jala.edu.catalog.application.usecase.EnableProductUseCase;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.core.infrastructure.ui.DisplayFormat;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;
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
    private final BooleanProperty disabling = new SimpleBooleanProperty(false);
    private final BooleanProperty enabling = new SimpleBooleanProperty(false);

    private final ListManagedProductsUseCase listManagedProductsUseCase;
    private final ListCommercesUseCase listCommercesUseCase;
    private final UpdateProductStatusUseCase updateProductStatusUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final DisableProductUseCase disableProductUseCase;
    private final EnableProductUseCase enableProductUseCase;
    private final CurrentUserService currentUserService;
    private final SpringFxmlLoader fxmlLoader;
    private static final Duration SEARCH_DELAY = Duration.millis(300);
    private final PauseTransition searchDelay = new PauseTransition(SEARCH_DELAY);

    // Evita que cargar las opciones del selector dispare otra recarga (bucle)
    private boolean updatingCommerceFilter;

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
    private Button disableButton;

    @FXML
    private Button enableButton;

    @FXML
    private ComboBox<ProductStatus> availabilitySelector;

    @FXML
    private Button updateAvailabilityButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CommerceSummary> commerceFilter;

    @FXML
    void initialize() {
        bindColumns();
        bindActionButtons();
        configureAvailabilitySelector();
        configureSelectionAndRows();
        configureCommerceFilter();
        configureSearch();
        reload(null);
    }

    private void bindColumns() {
        bind(nameColumn, managed -> managed.product().getName());
        bind(commerceColumn, ManagedProduct::commerceName);
        bind(categoryColumn, managed -> managed.product().getCategory());
        bind(priceColumn, managed -> managed.product().getPrice().toPlainString());
        bind(stockColumn, managed -> String.valueOf(managed.product().getStock()));
        bind(availabilityColumn, ManagedProduct::availability);
        bind(createdAtColumn, managed -> DisplayFormat.dateTime(managed.product().getCreatedAt()));
    }

    private void bindActionButtons() {
        // El botón Editar solo se habilita cuando hay un producto seleccionado
        editButton.disableProperty().bind(productsTable.getSelectionModel().selectedItemProperty().isNull());

        // Eliminar requiere un producto seleccionado y que no haya otra eliminación en curso
        deleteButton.disableProperty().bind(
                productsTable.getSelectionModel().selectedItemProperty().isNull().or(deleting));

        // Deshabilitar requiere un producto seleccionado que todavía esté habilitado
        disableButton.disableProperty().bind(
                productsTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(disabling)
                        .or(Bindings.createBooleanBinding(
                                () -> {
                                    ManagedProduct selected =
                                            productsTable.getSelectionModel().getSelectedItem();
                                    return selected != null
                                            && selected.product().getStatus() == ProductStatus.DISABLED;
                                },
                                productsTable.getSelectionModel().selectedItemProperty())));

        // Habilitar requiere un producto deshabilitado y que no haya otra habilitación en curso
        enableButton.disableProperty().bind(
                productsTable.getSelectionModel().selectedItemProperty().isNull()
                        .or(enabling)
                        .or(Bindings.createBooleanBinding(
                                () -> {
                                    ManagedProduct selected =
                                            productsTable.getSelectionModel().getSelectedItem();
                                    return selected == null
                                            || selected.product().getStatus() != ProductStatus.DISABLED;
                                },
                                productsTable.getSelectionModel().selectedItemProperty())));
    }

    private void configureAvailabilitySelector() {
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
    }

    private void configureSelectionAndRows() {
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
    }

    // El selector arranca solo con «Todos los comercios»; los comercios se cargan en reload(...)
    private void configureCommerceFilter() {
        commerceFilter.setConverter(new CommerceNameConverter());
        commerceFilter.getItems().setAll(CommerceFilterOptions.ALL);
        commerceFilter.setValue(CommerceFilterOptions.ALL);
        commerceFilter.valueProperty().addListener((observable, previous, selected) -> {
            if (!updatingCommerceFilter) {
                reload(null);
            }
        });
    }

    private void configureSearch() {
        searchDelay.setOnFinished(event -> reload(null));
        searchField.textProperty().addListener(
                (observable, previous, text) -> searchDelay.playFromStart());
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
     * no se ejecuta nada y el producto no se modifica. El solicitante es el usuario con sesión iniciada.
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

        UUID productId = selected.product().getId().value();

        deleting.set(true);

        // El usuario actual se consulta dentro de la tarea: si no hay sesión, el error llega al mensaje
        BackgroundTasks.run(
                () -> deleteProductUseCase.execute(
                        new DeleteProductCommand(currentUserService.currentUser().id(), productId)),
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
     * Deshabilita el producto seleccionado sin eliminarlo ni modificar su stock.
     */
    @FXML
    void onDisable() {
        ManagedProduct selected = productsTable.getSelectionModel().getSelectedItem();

        if (selected == null || selected.product().getStatus() == ProductStatus.DISABLED) {
            return;
        }

        boolean confirmed = Dialogs.confirm(
                productsTable,
                "¿Deshabilitar el producto " + selected.product().getName() + "?",
                "El producto dejará de estar disponible para los clientes. Se conservarán sus datos y su stock.");

        if (!confirmed) {
            return;
        }

        UUID productId = selected.product().getId().value();

        disabling.set(true);

        BackgroundTasks.run(
                () -> disableProductUseCase.execute(
                        new DisableProductCommand(currentUserService.currentUser().id(), productId)),
                done -> {
                    disabling.set(false);
                    reload("Product disabled successfully.");
                },
                error -> {
                    disabling.set(false);
                    showMessage(ErrorMessages.of(error, "deshabilitar el producto"), "error");
                });
    }

    /**
     * Habilita el producto seleccionado sin modificar sus datos ni su stock.
     */
    @FXML
    void onEnable() {
        ManagedProduct selected = productsTable.getSelectionModel().getSelectedItem();

        if (selected == null || selected.product().getStatus() != ProductStatus.DISABLED) {
            return;
        }

        boolean confirmed = Dialogs.confirm(
                productsTable,
                "¿Habilitar el producto " + selected.product().getName() + "?",
                "El producto volverá a estar disponible según su stock actual. Se conservarán sus datos y su stock.");

        if (!confirmed) {
            return;
        }

        UUID productId = selected.product().getId().value();

        enabling.set(true);

        BackgroundTasks.run(
                () -> enableProductUseCase.execute(
                        new EnableProductCommand(currentUserService.currentUser().id(), productId)),
                done -> {
                    enabling.set(false);
                    reload("Product enabled successfully.");
                },
                error -> {
                    enabling.set(false);
                    showMessage(ErrorMessages.of(error, "habilitar el producto"), "error");
                });
    }

    /**
     * Carga comercios y productos, conservando el comercio seleccionado; luego muestra
     * {@code successMessage}, o el conteo si es {@code null}.
     */
    private void reload(String successMessage) {
        String searchText = searchField.getText();
        UUID requestedCommerceId = selectedCommerceId();

        refreshButton.setDisable(true);
        BackgroundTasks.run(
                () -> loadScreenData(searchText, requestedCommerceId),
                data -> {
                    // Si el usuario cambió el texto o el comercio, esta respuesta ya quedó vieja
                    if (!Objects.equals(searchText, searchField.getText())
                            || !Objects.equals(requestedCommerceId, selectedCommerceId())) {
                        return;
                    }
                    showCommerceOptions(data.commerces(), data.commerceId());
                    productsTable.getItems().setAll(data.products());
                    showResultMessage(
                            successMessage, data.products(), searchText, data.commerceId() != null);
                    refreshButton.setDisable(false);
                },
                error -> {
                    showMessage(ErrorMessages.of(error, "load the products"), "error");
                    refreshButton.setDisable(false);
                });
    }

    /** Consulta comercios y productos; si el comercio pedido ya no existe, no se filtra. */
    private ScreenData loadScreenData(String searchText, UUID requestedCommerceId) {
        List<CommerceSummary> commerces = listCommercesUseCase.execute(ListCommercesQuery.all());
        UUID commerceId = CommerceFilterOptions.resolveId(commerces, requestedCommerceId);
        List<ManagedProduct> products = listManagedProductsUseCase.execute(
                new ListManagedProductsQuery(searchText, commerceId));
        return new ScreenData(commerces, commerceId, products);
    }

    /** Id del comercio seleccionado; {@code null} con «Todos los comercios». */
    private UUID selectedCommerceId() {
        return CommerceFilterOptions.idOf(commerceFilter.getValue());
    }

    /** Recarga las opciones del selector y deja elegido el comercio indicado. */
    private void showCommerceOptions(List<CommerceSummary> commerces, UUID commerceId) {
        List<CommerceSummary> options = CommerceFilterOptions.options(commerces);
        updatingCommerceFilter = true;
        try {
            commerceFilter.getItems().setAll(options);
            commerceFilter.setValue(CommerceFilterOptions.optionFor(options, commerceId));
        } finally {
            updatingCommerceFilter = false;
        }
    }

    private void showResultMessage(
            String successMessage, List<ManagedProduct> products,
            String searchText, boolean filteredByCommerce) {
        if (successMessage != null) {
            showMessage(successMessage, "success");
        } else if (products.isEmpty() && !searchText.isBlank()) {
            showMessage("No products match your search.", "info");
        } else if (products.isEmpty() && filteredByCommerce) {
            showMessage("El comercio no tiene productos.", "info");
        } else {
            showMessage(count(products.size()), "info");
        }
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

    /** Resultado de una carga: comercios del selector, comercio aplicado y productos filtrados. */
    private record ScreenData(
            List<CommerceSummary> commerces, UUID commerceId, List<ManagedProduct> products) {
    }
}