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

    private DeliveryViews() {
    }

}