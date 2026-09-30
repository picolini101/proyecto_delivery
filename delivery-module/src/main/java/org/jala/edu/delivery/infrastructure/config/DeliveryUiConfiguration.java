package org.jala.edu.delivery.infrastructure.config;

import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Adds the "Deliveries" button to the main window navigation bar; it opens the module's main view.
 */
@Configuration
public class DeliveryUiConfiguration {

    public static final String MAIN_VIEW = "/org/jala/edu/delivery/infrastructure/adapter/in/ui/delivery-main.fxml";

    @Bean
    @Order(400)
    public NavigationItem deliveryNavigationItem() {
        return new NavigationItem("Deliveries", MAIN_VIEW);
    }
}
