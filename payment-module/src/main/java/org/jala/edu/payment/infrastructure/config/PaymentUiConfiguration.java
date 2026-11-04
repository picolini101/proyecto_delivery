package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;


/**
 * Adds the "Payments" button to the main window navigation bar; it opens the module's main view.
 */
@Configuration

public class PaymentUiConfiguration {

    public static final String MAIN_VIEW = "/org/jala/edu/payment/infrastructure/adapter/in/ui/payment-main.fxml";

    @Bean
    @Order(500)
    public NavigationItem paymentNavigationItem() {
        return new NavigationItem("Payments", MAIN_VIEW);
    }
}
