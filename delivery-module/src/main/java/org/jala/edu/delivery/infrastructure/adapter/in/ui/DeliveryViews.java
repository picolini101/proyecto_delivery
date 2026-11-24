package org.jala.edu.delivery.infrastructure.adapter.in.ui;

/**
 * Paths to the FXML views of the module.
 * Classpath locations of the delivery module views.
 */
public final class DeliveryViews {

    private static final String BASE =
            "/org/jala/edu/delivery/infrastructure/adapter/in/ui/";

    /** Main view of the module, opened by the "Deliveries" navigation button. */
    public static final String MAIN = BASE + "delivery-main.fxml";

    /** History view of the delivery module. */
    public static final String HISTORY = BASE + "delivery-history.fxml";

    /** Courier registration view. */
    public static final String COURIER_REGISTRATION = BASE + "courier-registration.fxml";

    /** Ready orders view for the courier dashboard. */
    public static final String READY_ORDERS = BASE + "ready-orders.fxml";

    /** Shipment in progress, with the sequential delivery controls. */
    public static final String SHIPMENT = BASE + "shipment-progress.fxml";

    /** Customer's read-only view of the same shipment. */
    public static final String CUSTOMER_SHIPMENT = BASE + "customer-shipment.fxml";

    private DeliveryViews() {
    }

}