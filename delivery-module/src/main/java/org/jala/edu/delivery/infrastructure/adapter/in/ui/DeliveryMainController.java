package org.jala.edu.delivery.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Main view of the delivery module (view: {@code delivery-main.fxml}): the module's menu of screens.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class DeliveryMainController {

    private final Navigator navigator;

    @FXML
    void onReadyOrders() {
        navigator.show(DeliveryViews.READY_ORDERS);
    }

    @FXML
    void onShipment() {
        navigator.show(DeliveryViews.SHIPMENT);
    }

    @FXML
    void onDeliveryHistory() {
        navigator.show(DeliveryViews.HISTORY);
    }
}
