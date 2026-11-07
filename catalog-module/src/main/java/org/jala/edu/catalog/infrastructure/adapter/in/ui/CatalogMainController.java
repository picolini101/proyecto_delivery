package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
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
    private final CurrentUserService currentUserService;

    @FXML
    private HBox commerceActions;

    @FXML
    void initialize() {
        // Solo el administrador ve las acciones de comercios; la regla real está en los casos de uso
        boolean isAdmin = currentUserService.findCurrentUser()
                .map(user -> user.role() == UserRole.ADMIN)
                .orElse(false);
        commerceActions.setVisible(isAdmin);
        commerceActions.setManaged(isAdmin);
    }

    @FXML
    void onCreateCommerce() {
        navigator.show(CatalogViews.CREATE_COMMERCE);
    }

    @FXML
    void onManageCommerces() {
        navigator.show(CatalogViews.MANAGE_COMMERCES);
    }

    @FXML
    void onCreateProduct() {
        navigator.show(CatalogViews.CREATE_PRODUCT);
    }

    @FXML
    void onManageProducts() {
        navigator.show(CatalogViews.MANAGE_PRODUCTS);
    }

    @FXML
    void onViewCatalog() {
        navigator.show(CatalogViews.CUSTOMER_CATALOG);
    }
}