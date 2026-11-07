package org.jala.edu.order.infrastructure.config;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.usecase.AddProductToCartUseCase;
import org.jala.edu.order.application.usecase.DecreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.IncreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.ListCatalogProductsUseCase;
import org.jala.edu.order.application.usecase.RemoveProductFromCartUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso del modulo de pedidos en el contenedor de Spring.
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

    @Bean
    public IncreaseCartItemQuantityUseCase increaseCartItemQuantityUseCase(CartRepositoryPort carts,
                                                                           CatalogService catalog,
                                                                           UserService users) {
        return new IncreaseCartItemQuantityUseCase(carts, catalog, users);
    }

    @Bean
    public DecreaseCartItemQuantityUseCase decreaseCartItemQuantityUseCase(CartRepositoryPort carts,
                                                                           UserService users) {
        return new DecreaseCartItemQuantityUseCase(carts, users);
    }

    @Bean
    public RemoveProductFromCartUseCase removeProductFromCartUseCase(CartRepositoryPort carts) {
        return new RemoveProductFromCartUseCase(carts);
    }
}