package org.jala.edu.delivery.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    @ValueSource(strings = {
            DeliveryViews.MAIN,
            DeliveryViews.HISTORY,
            DeliveryViews.COURIER_REGISTRATION,
            DeliveryViews.READY_ORDERS,
            DeliveryViews.SHIPMENT,
            DeliveryViews.CUSTOMER_SHIPMENT
    })
    void everyDeliveryViewExistsOnTheClasspath(String fxmlPath) {
        // Arrange
        Class<?> moduleClass = DeliveryViews.class;

        // Act
        URL view = moduleClass.getResource(fxmlPath);

        // Assert
        assertThat(view).as("FXML on the classpath: %s", fxmlPath).isNotNull();
    }

    @Test
    void dashboardDeclaresTheEmptyStateAndTheTakeButton() throws Exception {
        // Arrange
        URL view = getClass().getResource(DeliveryViews.READY_ORDERS);

        // Act
        String fxml = new String(view.openStream().readAllBytes(), StandardCharsets.UTF_8);

        // Assert
        assertThat(fxml).contains("No hay pedidos disponibles en este momento");
        assertThat(fxml).contains("Tomar Pedido");
        assertThat(fxml).contains("Dirección");
        assertThat(fxml).contains("Costo de envío");
        assertThat(fxml).contains("En camino");
    }

    @Test
    void shipmentPanelDeclaresTheProgressControlsAndTheEmptyState() throws Exception {
        // Arrange
        URL view = getClass().getResource(DeliveryViews.SHIPMENT);

        // Act
        String fxml = new String(view.openStream().readAllBytes(), StandardCharsets.UTF_8);

        // Assert
        assertThat(fxml).contains("No hay un envío en curso");
        assertThat(fxml).contains("En camino");
        assertThat(fxml).contains("Retirado");
        assertThat(fxml).contains("Entregado");
        assertThat(fxml).contains("Código de confirmación");
        assertThat(fxml).contains("Cancelar");
        assertThat(fxml).contains("Delegar");
    }

    @Test
    void customerShipmentButtonOpensTheCustomerView() {
        // Act
        NavigationItem item = configuration.customerShipmentNavigationItem();
        URL view = DeliveryViews.class.getResource(item.fxmlPath());

        // Assert
        assertThat(item.title()).isEqualTo("My shipment");
        assertThat(item.fxmlPath()).isEqualTo(DeliveryViews.CUSTOMER_SHIPMENT);
        assertThat(view).isNotNull();
    }

    @Test
    void customerShipmentDeclaresTheProgressAndTheConfirmationCode() throws Exception {
        // Arrange
        URL view = getClass().getResource(DeliveryViews.CUSTOMER_SHIPMENT);

        // Act
        String fxml = new String(view.openStream().readAllBytes(), StandardCharsets.UTF_8);

        // Assert
        assertThat(fxml).contains("No hay un envío en curso");
        assertThat(fxml).contains("Código de confirmación");
        assertThat(fxml).contains("Actualizar");
    }
}
