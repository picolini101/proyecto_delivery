package org.jala.edu.delivery.infrastructure.config;

import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.jala.edu.delivery.infrastructure.adapter.in.ui.DeliveryViews;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Adds the "Deliveries" button to the main window navigation bar; it opens the module's main view.
 */
@Configuration
public class DeliveryUiConfiguration {

    public static final String MAIN_VIEW = DeliveryViews.MAIN;

    @Bean
    @Order(400)
    public NavigationItem deliveryNavigationItem() {
        return new NavigationItem("Deliveries", MAIN_VIEW);
    }
}
