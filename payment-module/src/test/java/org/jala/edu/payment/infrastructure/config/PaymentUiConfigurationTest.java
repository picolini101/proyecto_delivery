package org.jala.edu.payment.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.junit.jupiter.api.Test;

class PaymentUiConfigurationTest {

    private final PaymentUiConfiguration configuration = new PaymentUiConfiguration();

    @Test
    void navigationButtonIsLabelledPayments() {
        // Arrange
        String expectedTitle = "Payments";

        // Act
        NavigationItem item = configuration.paymentNavigationItem();

        // Assert
        assertThat(item.title()).isEqualTo(expectedTitle);
    }

    @Test
    void navigationButtonOpensAnExistingMainView() {
        // Arrange
        NavigationItem item = configuration.paymentNavigationItem();

        // Act
        URL view = getClass().getResource(item.fxmlPath());

        // Assert
        assertThat(item.fxmlPath()).isEqualTo(PaymentUiConfiguration.MAIN_VIEW);
        assertThat(view).as("FXML on the classpath: %s", item.fxmlPath()).isNotNull();
    }
}
