package org.jala.edu.order.infrastructure.adapter.in.ui;

/**
 * Ubicaciones en el classpath de las vistas del módulo de pedidos.
 */
public final class OrderViews {

    private static final String BASE = "/org/jala/edu/order/infrastructure/adapter/in/ui/";

    public static final String MAIN = BASE + "order-main.fxml";
    public static final String NEW_CART = BASE + "new-cart.fxml";
    public static final String ADDRESS = BASE + "address.fxml";
    public static final String PAYMENT_RESULT = BASE + "payment-result.fxml";
    public static final String TRACKING = BASE + "order-tracking.fxml";
    public static final String MERCHANT_ORDERS = BASE + "merchant-orders.fxml";

    public static final String CONFIRMATION = BASE + "order-confirmation.fxml";

    public static final String HISTORY = BASE + "order-history.fxml";
    private OrderViews() {
    }
}
