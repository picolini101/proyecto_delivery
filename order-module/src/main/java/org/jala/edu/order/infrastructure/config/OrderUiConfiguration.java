package org.jala.edu.order.infrastructure.config;

import org.jala.edu.order.infrastructure.adapter.in.ui.OrderViews;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Adds the "Orders" button to the main window navigation bar; it opens the module's main view.
 */
@Configuration
public class OrderUiConfiguration {

    public static final String MAIN_VIEW = "/org/jala/edu/order/infrastructure/adapter/in/ui/order-main.fxml";

    @Bean
    @Order(300)
    public NavigationItem orderNavigationItem() {
        return new NavigationItem("Orders", MAIN_VIEW);
    }

    @Bean
    @Order(310)
    public NavigationItem merchantOrdersNavigationItem() {
        return new NavigationItem("My commerce orders", OrderViews.MERCHANT_ORDERS);
    }
}
