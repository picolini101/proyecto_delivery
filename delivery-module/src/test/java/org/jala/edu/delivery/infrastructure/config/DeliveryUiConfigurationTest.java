package org.jala.edu.delivery.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.junit.jupiter.api.Test;

class DeliveryUiConfigurationTest {

    private final DeliveryUiConfiguration configuration = new DeliveryUiConfiguration();

    @Test
    void navigationButtonIsLabelledDeliveries() {
        // Arrange
        String expectedTitle = "Deliveries";

        // Act
        NavigationItem item = configuration.deliveryNavigationItem();

        // Assert
        assertThat(item.title()).isEqualTo(expectedTitle);
    }

    @Test
    void navigationButtonOpensAnExistingMainView() {
        // Arrange
        NavigationItem item = configuration.deliveryNavigationItem();

        // Act
        URL view = getClass().getResource(item.fxmlPath());

        // Assert
        assertThat(item.fxmlPath()).isEqualTo(DeliveryUiConfiguration.MAIN_VIEW);
        assertThat(view).as("FXML on the classpath: %s", item.fxmlPath()).isNotNull();
    }
}
