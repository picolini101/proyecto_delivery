package org.jala.edu.order.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class OrderMainController {

    private final Navigator navigator;

    @FXML
    void onNewCart() {
        navigator.show(OrderViews.NEW_CART);
    }
    @FXML
    void onManageCart() {
        navigator.show(OrderViews.REMOVE_ITEM);
    }
}