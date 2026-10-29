package org.jala.edu.order.infrastructure.config;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.usecase.AddProductToCartUseCase;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.ListCatalogProductsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso del modulo. Spring inyecta el catalogo y los usuarios reales,
 * o los fallbacks del core mientras esos módulos no los publiquen.
 */
@Configuration
public class OrderModuleConfiguration {

    @Bean
    public AddProductToCartUseCase addProductToCartUseCase(CartRepositoryPort carts, CatalogService catalog,
                                                           UserService users) {
        return new AddProductToCartUseCase(carts, catalog, users);
    }

    @Bean
    public GetCartSummaryUseCase getCartSummaryUseCase(CartRepositoryPort carts, CatalogService catalog) {
        return new GetCartSummaryUseCase(carts, catalog);
    }

    @Bean
    public ListCatalogProductsUseCase listCatalogProductsUseCase(CatalogService catalog) {
        return new ListCatalogProductsUseCase(catalog);
    }
}