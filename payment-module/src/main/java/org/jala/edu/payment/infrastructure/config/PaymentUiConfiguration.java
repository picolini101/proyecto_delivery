package org.jala.edu.payment.infrastructure.config;

import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Configuracion de las pantallas del modulo de pagos.
 */
@Configuration
public class PaymentUiConfiguration {

    // Pantalla principal: metodos, historial y procesamiento de pagos
    public static final String MAIN_VIEW =
            "/org/jala/edu/payment/infrastructure/adapter/in/ui/payment-home.fxml";

    @Bean
    @Order(500)
    public NavigationItem paymentNavigationItem() {
        return new NavigationItem("Payments", MAIN_VIEW);
    }
}
