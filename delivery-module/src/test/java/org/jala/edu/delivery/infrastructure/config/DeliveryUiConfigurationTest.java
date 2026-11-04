package org.jala.edu.delivery.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.jala.edu.delivery.infrastructure.adapter.in.ui.DeliveryViews;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryUiConfigurationTest {

    private final DeliveryUiConfiguration configuration =
            new DeliveryUiConfiguration();

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
        String path = item.fxmlPath();
        URL view = DeliveryViews.class.getResource(path);

        // Assert
        assertThat(path).isEqualTo(DeliveryViews.MAIN);
        assertThat(path).isEqualTo(DeliveryUiConfiguration.MAIN_VIEW);
        assertThat(view).as("FXML on the classpath: %s", path).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {DeliveryViews.MAIN, DeliveryViews.HISTORY, DeliveryViews.COURIER_REGISTRATION})
    void everyDeliveryViewExistsOnTheClasspath(String fxmlPath) {
        // Arrange
        Class<?> moduleClass = DeliveryViews.class;

        // Act
        URL view = moduleClass.getResource(fxmlPath);

        // Assert
        assertThat(view).as("FXML on the classpath: %s", fxmlPath).isNotNull();
    }
}