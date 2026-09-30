package org.jala.edu.order.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.junit.jupiter.api.Test;

class OrderUiConfigurationTest {

    private final OrderUiConfiguration configuration = new OrderUiConfiguration();

    @Test
    void navigationButtonIsLabelledOrders() {
        // Arrange
        String expectedTitle = "Orders";

        // Act
        NavigationItem item = configuration.orderNavigationItem();

        // Assert
        assertThat(item.title()).isEqualTo(expectedTitle);
    }

    @Test
    void navigationButtonOpensAnExistingMainView() {
        // Arrange
        NavigationItem item = configuration.orderNavigationItem();

        // Act
        URL view = getClass().getResource(item.fxmlPath());

        // Assert
        assertThat(item.fxmlPath()).isEqualTo(OrderUiConfiguration.MAIN_VIEW);
        assertThat(view).as("FXML on the classpath: %s", item.fxmlPath()).isNotNull();
    }
}
