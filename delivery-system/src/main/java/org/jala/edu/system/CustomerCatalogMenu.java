package org.jala.edu.system;

import org.jala.edu.catalog.infrastructure.adapter.in.ui.CatalogViews;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * "Browse catalog" button (#105): opens the read-only catalog of {@code catalog-module} (#50) for the customer,
 * who must not see the module's main view with Register and Manage products. {@link RoleMenu} shows it only to
 * CUSTOMER; ADMIN and MERCHANT keep "Catalog > View catalog".
 */
@Configuration
public class CustomerCatalogMenu {

    @Bean
    @Order(210)
    public NavigationItem customerCatalogNavigationItem() {
        return new NavigationItem("Browse catalog", CatalogViews.CUSTOMER_CATALOG);
    }
}
