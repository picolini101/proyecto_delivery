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

    // Pantalla principal del modulo de pagos
    public static final String MAIN_VIEW =
            "/org/jala/edu/payment/infrastructure/adapter/in/ui/payment-home.fxml";

    //procesamiento de pagos simulados
    public static final String PROCESS_PAYMENT_VIEW =
            "/org/jala/edu/payment/infrastructure/adapter/in/ui/process-payment.fxml";

    @Bean
    @Order(500)
    public NavigationItem paymentNavigationItem() {
        return new NavigationItem("Payments", MAIN_VIEW);
    }

    @Bean
    @Order(501)
    public NavigationItem processPaymentNavigationItem() {
        return new NavigationItem("Procesar pagos", PROCESS_PAYMENT_VIEW);
    }
}
