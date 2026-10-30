package org.jala.edu.order.infrastructure.adapter.in.ui;

import java.util.List;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.ui.BackgroundTasks;
import org.jala.edu.core.infrastructure.ui.ErrorMessages;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.jala.edu.order.application.usecase.AddProductToCartUseCase;
import org.jala.edu.order.application.usecase.CartSummary;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.ListCatalogProductsUseCase;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    /** Sin login hasta el sprint 2: el cliente actual es el de prueba. */
    private static final UUID CURRENT_CUSTOMER = SampleUsers.CUSTOMER;

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

    // Los campos con @FXML son elementos de new-cart.fxml; JavaFX los conecta solos por el nombre (fx:id).

    /** Contenedor donde se dibujan las tarjetas de productos. */
    @FXML
    private FlowPane productsPane;

    /** Contenedor donde se dibujan las lineas del carrito. */
    @FXML
    private VBox cartBox;

    /** Etiqueta para mostrar mensajes de exito o error. */
    @FXML
    private Label messageLabel;

    /** Texto "Tu carrito esta vacio"; solo se ve cuando no hay productos. */
    @FXML
    private Label emptyLabel;

    /** Texto del subtotal. */
    @FXML
    private Label subtotalLabel;

    /** Texto del envio. */
    @FXML
    private Label shippingLabel;

    /** Texto del total. */
    @FXML
    private Label totalLabel;

    /**
     * JavaFX llama a este metodo automaticamente cuando termina de cargar el FXML.
     * Carga los productos para dibujar sus tarjetas y luego dibuja el carrito actual.
     */
    @FXML
    void initialize() {
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
                () -> addProductToCart.execute(CURRENT_CUSTOMER, product.productId(), quantity),
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
                () -> getCartSummary.execute(CURRENT_CUSTOMER),
                summary -> showCart(summary),
                error -> showError(error, "cargar el carrito"));
    }

    /**
     * Dibuja el carrito: por cada comercio un titulo, y por cada linea "cantidad x nombre" con su subtotal.
     * Al final actualiza el subtotal, el envio y el total.
     *
     * @param summary resumen calculado por {@link GetCartSummaryUseCase}
     */
    private void showCart(CartSummary summary) {
        // Borra lo dibujado antes para no repetir lineas
        cartBox.getChildren().clear();
        for (CartSummary.Group group : summary.groups()) {
            Label header = new Label("Comercio " + shortId(group.commerceId()));
            header.setStyle("-fx-font-weight: bold; -fx-text-fill: #555555;");
            cartBox.getChildren().add(header);
            for (CartSummary.Line line : group.lines()) {
                Label item = new Label(line.quantity() + " x " + line.productName());
                // spacer: una caja vacia que se estira y empuja el precio hacia la derecha
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label amount = new Label(line.lineSubtotal().toString());
                cartBox.getChildren().add(new HBox(item, spacer, amount));
            }
        }

        // Muestra el texto "carrito vacio" solo si no hay productos.
        // setManaged(false) hace que el texto oculto tampoco ocupe espacio.
        boolean empty = summary.groups().isEmpty();
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);

        subtotalLabel.setText("Subtotal: " + summary.subtotal());
        shippingLabel.setText("Envío: " + summary.shipping());
        totalLabel.setText("Total: " + summary.total());
    }

    /**
     * Acorta un id para mostrarlo: devuelve solo los ultimos 4 caracteres.
     *
     * @param id id completo
     * @return los ultimos 4 caracteres del id
     */
    private static String shortId(UUID id) {
        return id.toString().substring(32);
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
    }
}