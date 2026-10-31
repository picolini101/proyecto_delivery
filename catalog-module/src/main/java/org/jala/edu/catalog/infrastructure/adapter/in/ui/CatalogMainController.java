package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.infrastructure.ui.Navigator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Vista principal del módulo de catálogo (vista: {@code catalog-main.fxml}): el menú de pantallas del módulo.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class CatalogMainController {

    private final Navigator navigator;

    @FXML
    void onCreateProduct() {
        navigator.show(CatalogViews.CREATE_PRODUCT);
    }
}