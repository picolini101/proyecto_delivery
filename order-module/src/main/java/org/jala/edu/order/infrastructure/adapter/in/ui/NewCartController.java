package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.SamplePaymentMethods;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.core.infrastructure.ui.SpringFxmlLoader;
import org.jala.edu.core.infrastructure.ui.Dialogs;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.AuthorizePaymentCommand;
import org.jala.edu.order.application.port.in.ConfirmOrderCommand;
import org.jala.edu.order.application.port.in.RegisterFailedPaymentCommand;
import org.jala.edu.order.application.usecase.orders.ConfirmOrderUseCase;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.application.usecase.orders.RegisterFailedPaymentUseCase;
import org.jala.edu.order.application.port.in.InputAddressCommand;
import org.jala.edu.order.application.port.in.StartCheckoutCommand;
import org.jala.edu.order.application.usecase.AddProductToCartUseCase;
import org.jala.edu.order.application.usecase.CartSummary;
import org.jala.edu.order.application.usecase.DecreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.IncreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.ListCatalogProductsUseCase;
import org.jala.edu.order.application.usecase.RemoveProductFromCartUseCase;
import org.jala.edu.order.application.usecase.orders.AuthorizePaymentUseCase;
import org.jala.edu.order.application.usecase.orders.InputAddressUseCase;
import org.jala.edu.order.application.usecase.orders.StartCheckoutUseCase;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.jala.edu.order.application.dto.CheckoutSummary;
import org.jala.edu.order.application.usecase.orders.ConfirmCartPricesUseCase;

import org.jala.edu.order.application.usecase.orders.PreviewCheckoutSummaryUseCase;
import org.jala.edu.order.application.service.CartVerification;
/**
 * Controlador de la pantalla de la tienda ({@code new-cart.fxml}).
 * A la izquierda muestra una tarjeta por cada producto del catalogo; a la derecha muestra el carrito del cliente.
 * El controlador no tiene reglas de negocio: solo llama a los casos de uso y dibuja el resultado.
 * <p>
 * Partes fundamentales:
 * <ul>
 *   <li>{@code @Component}: Spring crea este controlador.</li>
 *   <li>{@code @Scope(PROTOTYPE)}: se crea uno nuevo cada vez que se abre la pantalla.</li>
 *   <li>{@code @RequiredArgsConstructor} (Lombok): escribe solo el constructor con los campos {@code final}.</li>
 *   <li>{@code @FXML}: marca los campos y metodos que estan conectados con {@code new-cart.fxml}.</li>
 *   <li>{@code BackgroundTasks.run(trabajo, siSaleBien, siFalla)}: hace el trabajo en segundo plano para que la
 *       pantalla no se congele, y despues llama a "siSaleBien" o a "siFalla".</li>
 *   <li>La flecha {@code ->} (lambda) significa "este es un bloque de codigo que se ejecuta mas tarde".
 *       {@code () -> algo()} no recibe datos; {@code dato -> algo(dato)} recibe un dato.
 *       En este archivo cada flecha solo llama a un metodo, asi que se lee de izquierda a derecha.</li>
 * </ul>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class NewCartController {

    /** Cantidad maxima que se puede elegir con el boton "+". */
    private static final int MAX_QUANTITY = 99;

    /** Estilo (CSS de JavaFX) de la tarjeta de cada producto. */
    private static final String CARD_STYLE = "-fx-background-color: white; -fx-border-color: #d0d0d0; "
            + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;";

    /** Estilo de los botones azules principales. */
    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: #2563eb; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-background-radius: 6;";

    /** Estilo de los botones redondos "-" y "+". */
    private static final String ROUND_BUTTON_STYLE = "-fx-background-radius: 14; -fx-min-width: 28; "
            + "-fx-min-height: 28; -fx-font-weight: bold;";

    /** Caso de uso: agregar un producto al carrito. */
    private final AddProductToCartUseCase addProductToCart;

    /** Caso de uso: calcular el resumen del carrito. */
    private final GetCartSummaryUseCase getCartSummary;

    /** Caso de uso: listar los productos de la tienda. */
    private final ListCatalogProductsUseCase listCatalogProducts;

    /** Para volver a la pantalla anterior. */
    private final Navigator navigator;

    private final CurrentUserService currentUserService;

    private final IncreaseCartItemQuantityUseCase increaseQuantity;

    private final DecreaseCartItemQuantityUseCase decreaseQuantity;

    private final RemoveProductFromCartUseCase removeProductFromCart;

    private final StartCheckoutUseCase startCheckout;
    private final InputAddressUseCase inputAddress;

    /** pide al servicio de pagos que autorice el monto total del pedido. */
    private final AuthorizePaymentUseCase authorizePayment;

    /** Cancela el pedido y registra el intento fallido cuando el pago es rechazado. */
    private final RegisterFailedPaymentUseCase registerFailedPayment;
    private final ConfirmOrderUseCase confirmOrder;
    private final OrderConfirmationContext orderConfirmationContext;


    /** Decide los textos de la ventana de respuesta de pago. Al tener valor inicial, Lombok no lo pide en el constructor. */
    private final PaymentResultPresenter paymentPresenter = new PaymentResultPresenter();
    private final SpringFxmlLoader fxmlLoader;
    private DeliveryAddress deliveryAddress;
    private UUID lastOrderId;

    private final PreviewCheckoutSummaryUseCase previewCheckoutSummary;
    // Los campos con @FXML son elementos de new-cart.fxml; JavaFX los conecta solos por el nombre (fx:id).

    /** Guarda en el carrito los precios nuevos que el cliente acepta. */
    private final ConfirmCartPricesUseCase confirmCartPrices;

    /** Contenedor donde se dibujan las tarjetas de productos. */
    @FXML
    private FlowPane productsPane;

    /** Contenedor donde se dibujan las lineas del carrito. */
    @FXML private ScrollPane productsView;
    @FXML private VBox cartView;
    @FXML private TableView<CartSummary.Line> cartTable;
    @FXML private TableColumn<CartSummary.Line, String> productColumn;
    @FXML private TableColumn<CartSummary.Line, Integer> numberColumn;
    @FXML private TableColumn<CartSummary.Line, Integer> quantityColumn;
    @FXML private TableColumn<CartSummary.Line, String> unitPriceColumn;
    @FXML private TableColumn<CartSummary.Line, String> subtotalColumn;
    @FXML private TableColumn<CartSummary.Line, Void> actionColumn;
    @FXML private VBox tableContent;
    @FXML private VBox emptyState;
    @FXML private HBox shippingRow;
    @FXML private HBox totalRow;
    @FXML private Label pageTitle;
    @FXML private Button showCartButton;
    @FXML private Button tableAddProductButton;
    @FXML private Button emptyAddProductButton;
    @FXML private Button addAddressButton;
    @FXML private Label deliveryAddressLabel;

    /** Etiqueta para mostrar mensajes de exito o error. */
    @FXML
    private Label messageLabel;

    /** Texto "Tu carrito esta vacio"; solo se ve cuando no hay productos. */
    @FXML
    private Label emptyLabel;

    @FXML
    private Button checkoutButton;

    /**
     * JavaFX llama a este metodo automaticamente cuando termina de cargar el FXML.
     * Carga los productos para dibujar sus tarjetas y luego dibuja el carrito actual.
     */
    @FXML
    void initialize() {
        checkoutButton.setDisable(true);
        configureTable();
        BackgroundTasks.run(
                () -> listCatalogProducts.execute(),
                products -> showProducts(products),
                error -> showError(error, "cargar el catálogo"));
        refreshCart();
    }

    /** Se ejecuta al presionar "Volver a pedidos": regresa a la pantalla principal de pedidos. */
    @FXML
    void onBack() {
        navigator.show(OrderViews.MAIN);
    }

    @FXML void onShowCart() {
        clearMessage();
        setVisibleAndManaged(productsView, false);
        setVisibleAndManaged(cartView, true);
        setVisibleAndManaged(pageTitle, false);
        setVisibleAndManaged(showCartButton, false);
        refreshCart();
    }

    @FXML void onAddProduct() {
        setVisibleAndManaged(cartView, false);
        setVisibleAndManaged(productsView, true);
        setVisibleAndManaged(messageLabel, false);
        setVisibleAndManaged(pageTitle, true);
        setVisibleAndManaged(showCartButton, true);
    }

    /** Muestra u oculta un nodo y libera o reserva su espacio en el layout. */
    private static void setVisibleAndManaged(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    @FXML
    void onStartCheckout() {
        if (deliveryAddress == null) {
            checkoutButton.setDisable(true);
            return;
        }
        checkoutButton.setDisable(true);

        StartCheckoutCommand command = new StartCheckoutCommand(currentUserService.currentUser().id());

        BackgroundTasks.run(
                () -> previewCheckoutSummary.execute(command),
                summary -> {
                    if (!summary.isValid()) {
                        showMessage("Alerta: Algunos productos cambiaron de disponibilidad. Revisa tu carrito.", "error");
                        checkoutButton.setDisable(false);
                        return;
                    }

                    if (summary.hasPriceChanges()) {
                        askToConfirmPrices(summary);
                        return;
                    }

                    // Armamos el detalle de productos
                    StringBuilder detalle = new StringBuilder();
                    for (CartVerification.Line line : summary.lines()) {
                        detalle.append(String.format("%dx %s - $%s\n",
                                line.quantity(),
                                line.productName(),
                                line.lineSubtotal()));
                    }
                    detalle.append("-------------------------\n");

                    // Mostramos el modal con el total
                    boolean confirm = Dialogs.confirm(checkoutButton, "Resumen de Compra",
                            detalle.toString() +
                                    "Subtotal: $" + summary.subtotal() + "\n" +
                                    "Envío: $" + summary.shippingCost() + "\n" +
                                    "TOTAL A PAGAR: $" + summary.total() + "\n\n" +
                                    "¿Desea autorizar el pago?");

                    if (confirm) {
                        // Si el usuario acepta, procedemos con la creación de la orden y el pago
                        placeOrder(command);
                    } else {
                        checkoutButton.setDisable(false);
                    }
                },
                error -> {
                    showError(error, "calcular el resumen");
                    checkoutButton.setDisable(false);
                }
        );
    }

    /** Crea la orden y pasa al pago cuando el usuario confirma el resumen. */
    private void placeOrder(StartCheckoutCommand command) {
        BackgroundTasks.run(
                () -> startCheckout.execute(command),
                orderSummary -> {
                    lastOrderId = orderSummary.orderId();
                    refreshCart();
                    if (deliveryAddress != null) {
                        saveAddressForOrder(lastOrderId, deliveryAddress);
                    }
                    // Llamamos a tu metodo que muestra el modal verde de "Pago aprobado"
                    requestPayment();
                },
                error -> {
                    showError(error, "iniciar la compra");
                    checkoutButton.setDisable(false);
                }
        );
    }

    /**
     * El precio de algun producto cambio desde que se agrego al carrito: muestra el precio anterior y el nuevo
     * y pide confirmar. Si el cliente acepta, se guardan los precios nuevos y se vuelve a mostrar el resumen de
     * la compra. Si no acepta, se queda en el carrito para que cambie la cantidad o quite el producto.
     *
     * @param summary resumen que trae los cambios de precio
     */
    private void askToConfirmPrices(CheckoutSummary summary) {
        StringBuilder detalle = new StringBuilder();
        for (CartVerification.PriceChange change : summary.priceChanges()) {
            detalle.append(String.format("%s: de $%s a $%s%n", change.productName(),
                    change.oldPrice().toPlainString(), change.newPrice().toPlainString()));
        }
        boolean accepted = Dialogs.confirm(checkoutButton, "El precio de un producto cambió",
                detalle + "\n¿Aceptas los nuevos precios?\n"
                        + "Si no los aceptas, puedes cambiar la cantidad o quitar el producto del carrito.");

        if (!accepted) {
            showMessage("No aceptaste el nuevo precio. Cambia la cantidad o quita el producto del carrito.", "error");
            refreshCart();
            checkoutButton.setDisable(false);
            return;
        }

        UUID customerId = currentUserService.currentUser().id();
        BackgroundTasks.run(
                () -> confirmCartPrices.execute(customerId, summary.priceChanges()),
                cart -> onStartCheckout(),
                error -> {
                    showError(error, "confirmar los nuevos precios");
                    checkoutButton.setDisable(false);
                });
    }

    /**
     * Pide la autorizacion del pago del ultimo pedido creado, en segundo plano, y muestra el resultado
     * (aprobado, rechazado o problema temporal) en una ventana. Tambien lo usa el boton "Reintentar pago".
     * Mientras no exista una pantalla para elegir el metodo de pago, se usa la tarjeta de prueba del cliente.
     */
    private void requestPayment() {
        AuthorizePaymentCommand command = new AuthorizePaymentCommand(
                lastOrderId, currentUserService.currentUser().id(), SamplePaymentMethods.CUSTOMER_CARD);
        BackgroundTasks.run(
                () -> authorizePayment.execute(command),
                this::handleAttempt,
                error -> showPaymentResult(paymentPresenter.presentFailure(error)));
    }

    /**
     * Si el pago fue rechazado, primero cancela el pedido y guarda el intento fallido; despues muestra el resultado.
     * Aprobado o servicio caido no cancelan nada (el pedido sigue en CREATED y se puede reintentar).
     */
    private void handleAttempt(PaymentAttempt attempt) {
        if (attempt.authorization() instanceof PaymentAuthorization.Rejected) {
            BackgroundTasks.run(
                    () -> registerFailedPayment.execute(
                            new RegisterFailedPaymentCommand(attempt, currentUserService.currentUser().id())),
                    ignored -> showPaymentResult(paymentPresenter.present(attempt)),
                    error -> showPaymentResult(paymentPresenter.presentFailure(error)));
        } else if (attempt.authorization() instanceof PaymentAuthorization.Approved) {
            ConfirmOrderCommand confirmCommand = new ConfirmOrderCommand(
                    lastOrderId, currentUserService.currentUser().id(), attempt);
            BackgroundTasks.run(
                    () -> confirmOrder.execute(confirmCommand),
                    ignored -> showPaymentResult(paymentPresenter.present(attempt)),
                    error -> showError(error, "confirmar el pedido"));
        } else {
            showPaymentResult(paymentPresenter.present(attempt));
        }
    }

    /**
     * Abre la ventana de respuesta de pago. Si el pago fue aprobado, el boton principal ("Continuar") lleva a la
     * pantalla de pedidos. Si no, el cliente sigue en esta pantalla y el boton principal reintenta el pago.
     *
     * @param result lo que debe mostrarse
     */

    private void showPaymentResult(PaymentResultView result) {
        SpringFxmlLoader.View<PaymentResultController> view = fxmlLoader.loadView(OrderViews.PAYMENT_RESULT);
        Runnable action;
        if (result.kind() == PaymentResultView.Kind.UNAVAILABLE) {
            action = this::requestPayment;
        } else if (result.isSuccess() && lastOrderId != null) {
            // Si se aprobó, mostramos el comprobante final con el código de entrega
            action = () -> {
                orderConfirmationContext.setLastConfirmedOrderId(lastOrderId);
                navigator.show(OrderViews.CONFIRMATION);
            };
        }
        else {
            action = () -> navigator.show(OrderViews.MAIN);
        }
        view.controller().prepare(result, action);
        Dialogs.showModal(checkoutButton, result.title(), view.root());
    }


    @FXML
    void onAddAddress() {
        SpringFxmlLoader.View<InputAddressController> view = fxmlLoader.loadView(OrderViews.ADDRESS);
        view.controller().prepare(deliveryAddress, this::addressConfirmed);
        Dialogs.showModal(addAddressButton, "Ingrese la Dirección de Entrega", view.root());
    }

    private void addressConfirmed(DeliveryAddress address) {
        deliveryAddress = address;
        deliveryAddressLabel.setText(address.toString());
        checkoutButton.setDisable(false);
        if (lastOrderId != null) {
            saveAddressForOrder(lastOrderId, address);
        }
    }

    private void saveAddressForOrder(UUID orderId, DeliveryAddress address) {
        InputAddressCommand command = new InputAddressCommand(
                orderId, currentUserService.currentUser().id(), address.city(), address.avenue(), address.houseNumber());
        BackgroundTasks.run(
                () -> inputAddress.execute(command),
                ignored -> showMessage("Dirección de entrega guardada", "success"),
                error -> showError(error, "guardar la dirección de entrega"));
    }

    private void configureTable() {
        cartTable.skinProperty().addListener((observable, previousSkin, currentSkin) ->
                javafx.application.Platform.runLater(this::styleTableHeaders));
        setPrimaryButtonHover(tableAddProductButton);
        setPrimaryButtonHover(emptyAddProductButton);
        configureRows();
        configureDataColumns();
        configureQuantityColumn();
        configureActionColumn();
        cartTable.setFixedCellSize(56);
    }

    private void configureRows() {
        cartTable.setRowFactory(table -> new javafx.scene.control.TableRow<>() {
            @Override protected void updateItem(CartSummary.Line item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(empty ? "" : (getIndex() % 2 == 0
                        ? "-fx-background-color: white; -fx-border-color: transparent transparent #eef2f7 transparent;"
                        : "-fx-background-color: #fbfdff; -fx-border-color: transparent transparent #eef2f7 transparent;"));
            }
        });
    }

    private void configureDataColumns() {
        numberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(
                data.getTableView().getItems().indexOf(data.getValue()) + 1));
        numberColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
                setAlignment(Pos.CENTER);
                setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #334155;");
            }
        });
        productColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().productName()));
        unitPriceColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().unitPrice() + "$"));
        subtotalColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().lineSubtotal() + "$"));
        styleTextColumn(productColumn);
        styleTextColumn(unitPriceColumn);
        styleTextColumn(subtotalColumn);
    }

    private void configureQuantityColumn() {
        quantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer ignored, boolean empty) {
                super.updateItem(ignored, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                CartSummary.Line line = getTableView().getItems().get(getIndex());
                HBox controls = new HBox(10);
                controls.setAlignment(Pos.CENTER);
                controls.setStyle("-fx-padding: 3 5; -fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; "
                        + "-fx-border-radius: 9; -fx-background-radius: 9;");
                Button minus = new Button("−");
                styleStepperButton(minus);
                minus.setDisable(line.quantity() <= 1);
                Label count = new Label(String.valueOf(line.quantity()));
                count.setStyle("-fx-min-width: 28px; -fx-alignment: center; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
                Button plus = new Button("+");
                styleStepperButton(plus);
                controls.getChildren().setAll(minus, count, plus);
                minus.setOnAction(event -> changeLine(line, controls, false));
                plus.setOnAction(event -> changeLine(line, controls, true));
                setGraphic(controls);
            }
        });
        quantityColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().quantity()));
    }

    private void configureActionColumn() {
        // Configuracion de la columna de eliminacion (HU-02)
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Eliminar");

            {
                // Estilo rojo moderno acorde a la paleta de la aplicacion
                String normalStyle = "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; "
                        + "-fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 7; -fx-cursor: hand;";
                String hoverStyle = "-fx-background-color: #fecaca; -fx-text-fill: #b91c1c; -fx-font-weight: bold; "
                        + "-fx-font-size: 12px; -fx-padding: 6 14; -fx-background-radius: 7; -fx-cursor: hand;";

                deleteButton.setStyle(normalStyle);
                deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(hoverStyle));
                deleteButton.setOnMouseExited(e -> deleteButton.setStyle(normalStyle));

                deleteButton.setOnAction(event -> {
                    CartSummary.Line line = getTableView().getItems().get(getIndex());
                    // Confirmacion modal obligatoria mediante el contrato central de Dialogs
                    boolean confirmed = org.jala.edu.core.infrastructure.ui.Dialogs.confirm(
                            deleteButton,
                            "Eliminar producto",
                            "¿Está seguro de que desea eliminar \"" + line.productName() + "\" del carrito?"
                    );
                    if (confirmed) {
                        deleteButton.setDisable(true);
                        BackgroundTasks.run(
                                () -> removeProductFromCart.execute(currentUserService.currentUser().id(), line.productId()),
                                updatedCart -> {
                                    showMessage("Producto eliminado del carrito", "success");
                                    refreshCart();
                                },
                                error -> {
                                    showError(error, "eliminar producto");
                                    deleteButton.setDisable(false);
                                }
                        );
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setAlignment(Pos.CENTER);
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void styleTableHeaders() {
        for (javafx.scene.Node header : cartTable.lookupAll(".column-header")) {
            header.setStyle("-fx-background-color: #f1f5f9; -fx-size: 42px; "
                    + "-fx-border-color: transparent transparent #e2e8f0 transparent;");
        }
        for (javafx.scene.Node label : cartTable.lookupAll(".column-header .label")) {
            label.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
        for (javafx.scene.Node filler : cartTable.lookupAll(".filler")) {
            filler.setStyle("-fx-background-color: #f1f5f9;");
        }
    }

    private void styleTextColumn(TableColumn<CartSummary.Line, String> column) {
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
                setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: #334155;");
            }
        });
    }

    private void setPrimaryButtonHover(Button button) {
        String normal = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; "
                + "-fx-padding: 11 24; -fx-background-radius: 9; -fx-cursor: hand;";
        String hover = "-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; "
                + "-fx-padding: 11 24; -fx-background-radius: 9; -fx-cursor: hand; "
                + "-fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.25), 9, 0.15, 0, 3);";
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }

    private void styleStepperButton(Button button) {
        String normal = "-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-weight: bold; -fx-font-size: 15px; "
                + "-fx-min-width: 30px; -fx-min-height: 30px; -fx-background-radius: 7; -fx-cursor: hand;";
        String hover = "-fx-background-color: #dbeafe; -fx-text-fill: #1d4ed8; -fx-font-weight: bold; -fx-font-size: 15px; "
                + "-fx-min-width: 30px; -fx-min-height: 30px; -fx-background-radius: 7; -fx-cursor: hand;";
        button.setStyle(normal);
        button.setOnMouseEntered(event -> button.setStyle(hover));
        button.setOnMouseExited(event -> button.setStyle(normal));
    }

    /**
     * Dibuja una tarjeta por cada producto.
     *
     * @param products productos que devolvio el catalogo
     */
    private void showProducts(List<ProductInfo> products) {
        for (ProductInfo product : products) {
            productsPane.getChildren().add(buildCard(product));
        }
    }

    /**
     * Construye la tarjeta de un producto: una letra grande, el nombre, el precio y la zona del boton.
     *
     * @param product producto a mostrar
     * @return la tarjeta lista para agregarla a la pantalla
     */
    private VBox buildCard(ProductInfo product) {
        // "Imagen" falsa: un cuadro con la primera letra del nombre
        Label initial = new Label(product.name().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");
        StackPane picture = new StackPane(initial);
        picture.setPrefSize(140, 90);
        picture.setStyle("-fx-background-color: #e8effd; -fx-background-radius: 6;");

        Label name = new Label(product.name());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        Label price = new Label(product.price().toString());
        price.setStyle("-fx-text-fill: #555555;");

        // "action" es la zona de abajo de la tarjeta: ahi se cambia entre el boton y el contador
        StackPane action = new StackPane();
        action.setPrefHeight(64);
        showAddButton(action, product);

        VBox card = new VBox(6, picture, name, price, action);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(160);
        card.setMinWidth(160);
        card.setStyle(CARD_STYLE);
        return card;
    }

    /**
     * Muestra el boton "Agregar al carrito" en la zona de accion de la tarjeta.
     * Al presionarlo, el boton se reemplaza por el contador de cantidad.
     *
     * @param action  zona de la tarjeta donde se pone el boton
     * @param product producto de la tarjeta
     */
    private void showAddButton(StackPane action, ProductInfo product) {
        Button add = new Button("Agregar al carrito");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setStyle(PRIMARY_BUTTON_STYLE);
        add.setOnAction(event -> showCounter(action, product));
        // setAll reemplaza lo que hubiera en la zona por este boton
        action.getChildren().setAll(add);
    }

    /**
     * Muestra el contador de cantidad ("-", numero, "+") con los botones Aceptar y Cancelar.
     * La cantidad elegida se guarda en el texto del numero. Aceptar agrega el producto al carrito;
     * Cancelar vuelve al boton "Agregar al carrito".
     *
     * @param action  zona de la tarjeta donde se pone el contador
     * @param product producto de la tarjeta
     */
    private void showCounter(StackPane action, ProductInfo product) {
        Label count = new Label("1");
        count.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 28; -fx-alignment: center;");

        Button minus = new Button("−");
        minus.setStyle(ROUND_BUTTON_STYLE);
        minus.setOnAction(event -> changeCount(count, -1));
        Button plus = new Button("+");
        plus.setStyle(ROUND_BUTTON_STYLE);
        plus.setOnAction(event -> changeCount(count, 1));
        HBox stepper = new HBox(8, minus, count, plus);
        stepper.setAlignment(Pos.CENTER);

        Button accept = new Button("Aceptar");
        accept.setStyle(PRIMARY_BUTTON_STYLE);
        // Aceptar: lee la cantidad del texto del numero y agrega el producto
        accept.setOnAction(event -> addToCart(product, Integer.parseInt(count.getText()), accept, action));
        Button cancel = new Button("Cancelar");
        cancel.setOnAction(event -> showAddButton(action, product));
        HBox buttons = new HBox(6, accept, cancel);
        buttons.setAlignment(Pos.CENTER);

        action.getChildren().setAll(new VBox(6, stepper, buttons));
    }

    /**
     * Cambia el numero del contador. Nunca baja de 1 ni sube de MAX_QUANTITY.
     *
     * @param count  etiqueta que muestra la cantidad
     * @param change cuanto cambiar: -1 para bajar, 1 para subir
     */
    private void changeCount(Label count, int change) {
        int newValue = Integer.parseInt(count.getText()) + change;
        if (newValue < 1) {
            newValue = 1;
        }
        if (newValue > MAX_QUANTITY) {
            newValue = MAX_QUANTITY;
        }
        count.setText(String.valueOf(newValue));
    }

    /**
     * Agrega el producto al carrito usando el caso de uso (en segundo plano).
     * Mientras se agrega, el boton Aceptar queda desactivado para que no se presione dos veces.
     *
     * @param product  producto a agregar
     * @param quantity cantidad elegida
     * @param accept   boton Aceptar de la tarjeta
     * @param action   zona de la tarjeta donde esta el contador
     */
    private void addToCart(ProductInfo product, int quantity, Button accept, StackPane action) {
        accept.setDisable(true);
        BackgroundTasks.run(
                () -> addProductToCart.execute(currentUserService.currentUser().id(), product.productId(), quantity),
                cart -> productAdded(product, quantity, action),
                error -> addFailed(error, accept));
    }

    /**
     * Se llama cuando el producto ya se agrego: muestra el mensaje de exito, vuelve al boton
     * "Agregar al carrito" y vuelve a dibujar el carrito.
     *
     * @param product  producto agregado
     * @param quantity cantidad agregada
     * @param action   zona de la tarjeta donde se vuelve a poner el boton
     */
    private void productAdded(ProductInfo product, int quantity, StackPane action) {
        showMessage(quantity + " x " + product.name() + " agregado al carrito", "success");
        showAddButton(action, product);
        refreshCart();
    }

    /**
     * Se llama cuando no se pudo agregar (por ejemplo sin stock): muestra el error y activa otra vez el boton Aceptar.
     *
     * @param error  el error que ocurrio
     * @param accept boton Aceptar de la tarjeta
     */
    private void addFailed(Throwable error, Button accept) {
        showError(error, "agregar al carrito");
        accept.setDisable(false);
    }

    /** Pide el resumen del carrito al caso de uso (en segundo plano) y lo dibuja en pantalla. */
    private void refreshCart() {
        BackgroundTasks.run(
                () -> getCartSummary.execute(currentUserService.currentUser().id()),
                summary -> showCart(summary),
                error -> showError(error, "cargar el carrito"));
    }

    /**
     * Dibuja el carrito: por cada comercio un titulo, y por cada linea el nombre, el subtotal y los botones
     * "-" y "+" para cambiar la cantidad.
     * Al final actualiza el subtotal, el envio y el total.
     *
     * @param summary resumen calculado por {@link GetCartSummaryUseCase}
     */
    private void showCart(CartSummary summary) {
        List<CartSummary.Line> lines = summary.groups().stream().flatMap(group -> group.lines().stream()).toList();
        cartTable.setItems(FXCollections.observableArrayList(lines));
        double tableHeight = cartTable.getFixedCellSize() * lines.size() + 48;
        cartTable.setMinHeight(tableHeight);
        cartTable.setPrefHeight(tableHeight);
        cartTable.setMaxHeight(tableHeight);
        boolean empty = lines.isEmpty();
        tableContent.setVisible(!empty);
        tableContent.setManaged(!empty);
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        shippingRow.getChildren().setAll(summaryRow("ENVIO", summary.shipping() + "$", false));
        totalRow.getChildren().setAll(summaryRow("TOTAL", summary.total() + "$", true));
    }

    private HBox summaryRow(String title, String amount, boolean total) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label label = new Label(title);
        Label value = new Label(amount);
        String textStyle = total
                ? "-fx-text-fill: #1b4f72; -fx-font-size: 16px; -fx-font-weight: bold;"
                : "-fx-text-fill: #475569; -fx-font-size: 14px;";
        label.setStyle(textStyle);
        value.setStyle(textStyle);
        HBox row = new HBox(24, spacer, label, value);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setStyle(total
                ? "-fx-padding: 12 18; -fx-background-color: transparent; -fx-border-color: #c7dceb transparent transparent transparent;"
                : "-fx-padding: 12 18; -fx-background-color: transparent;");
        return row;
    }

    private void changeLine(CartSummary.Line line, HBox controls, boolean increase) {
        controls.setDisable(true);
        BackgroundTasks.run(
                () -> increase
                        ? increaseQuantity.execute(currentUserService.currentUser().id(), line.productId(), 1)
                        : decreaseQuantity.execute(currentUserService.currentUser().id(), line.productId(), 1),
                cart -> refreshCart(),
                error -> {
                    showError(error, "actualizar cantidad");
                    controls.setDisable(false);
                });
    }

    /**
     * Muestra un error arriba de la pantalla. Si el error es una regla del negocio (por ejemplo sin stock)
     * muestra su mensaje; si es otro error muestra un mensaje general.
     *
     * @param error  el error que ocurrio
     * @param action lo que se estaba intentando hacer, por ejemplo "agregar al carrito"
     */
    private void showError(Throwable error, String action) {
        showMessage(ErrorMessages.of(error, action), "error");
    }

    /**
     * Muestra un mensaje arriba de la pantalla.
     *
     * @param text       texto del mensaje
     * @param styleClass tipo de mensaje: "info", "success" o "error" (cambia el color)
     */
    private void showMessage(String text, String styleClass) {
        // Quita el estilo anterior para que no se mezclen los colores
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
        messageLabel.setStyle(styleClass.equals("error")
                ? "-fx-padding: 9 14; -fx-background-color: #fef2f2; -fx-background-radius: 8; "
                + "-fx-font-size: 13px; -fx-text-fill: #b91c1c;"
                : "-fx-padding: 9 14; -fx-background-color: #eff6ff; -fx-background-radius: 8; "
                + "-fx-font-size: 13px; -fx-text-fill: #15803d;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        PauseTransition timeout = new PauseTransition(Duration.seconds(3));
        timeout.setOnFinished(event -> {
            if (text.equals(messageLabel.getText())) {
                clearMessage();
            }
        });
        timeout.play();
    }

    /** Borra el mensaje de arriba: quita el texto y el color */
    public void clearMessage(){
        messageLabel.getStyleClass().removeAll("info", "success", "error");
        messageLabel.setText("");
        messageLabel.setStyle("-fx-padding: 9 14; -fx-background-color: #eff6ff; -fx-background-radius: 8; -fx-font-size: 13px;");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

}