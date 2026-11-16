package org.jala.edu.catalog.infrastructure.config;

import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.application.usecase.*;
import org.jala.edu.catalog.domain.matching.AccentInsensitiveStrategy;
import org.jala.edu.catalog.domain.matching.TextMatchStrategy;
import org.jala.edu.core.contract.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Registra los casos de uso del catálogo como beans. Los casos de uso son clases simples
 * (sin anotaciones de Spring), por eso se construyen aquí con sus puertos.
 */
@Configuration
public class CatalogModuleConfiguration {

    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepositoryPort productRepositoryPort,
            CommerceRepositoryPort commerceRepositoryPort,
            UserService userService) {
        return new CreateProductUseCase(productRepositoryPort, commerceRepositoryPort, userService);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepositoryPort productRepositoryPort,
                                                     UserService userService) {
        return new UpdateProductUseCase(productRepositoryPort, userService);
    }

    @Bean
    public ListManagedProductsUseCase listManagedProductsUseCase(
            ProductRepositoryPort productRepositoryPort,
            CommerceRepositoryPort commerceRepositoryPort,
            TextMatchStrategy textMatchStrategy) {
        return new ListManagedProductsUseCase(
                productRepositoryPort, commerceRepositoryPort, textMatchStrategy);
    }

    @Bean
    public AdjustProductStockUseCase adjustProductStockUseCase(
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new AdjustProductStockUseCase(productRepositoryPort, userService);
    }

    @Bean
    public UpdateProductStatusUseCase updateProductStatusUseCase(
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new UpdateProductStatusUseCase(productRepositoryPort, userService);
    }

    @Bean
    public DisableProductUseCase disableProductUseCase(
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new DisableProductUseCase(productRepositoryPort, userService);
    }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepositoryPort productRepositoryPort,
            CommerceRepositoryPort commerceRepositoryPort,
            UserService userService) {
        return new DeleteProductUseCase(productRepositoryPort, commerceRepositoryPort, userService);
    }

    @Bean
    public ListAvailableProductsUseCase listAvailableProductsUseCase(
            ProductRepositoryPort productRepositoryPort,
            CommerceRepositoryPort commerceRepositoryPort,
            @Value("${catalog.pagination.page-size:10}") int pageSize) {
        return new ListAvailableProductsUseCase(
                productRepositoryPort,
                commerceRepositoryPort,
                pageSize);
    }

    @Bean
    public RegisterCommerceUseCase registerCommerceUseCase(
            CommerceRepositoryPort commerceRepositoryPort,
            UserService userService) {
        return new RegisterCommerceUseCase(commerceRepositoryPort, userService);
    }

    @Bean
    public EnableCommerceUseCase enableCommerceUseCase(
            CommerceRepositoryPort commerceRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new EnableCommerceUseCase(
                commerceRepositoryPort, productRepositoryPort, userService);
    }

    @Bean
    public ListCommercesUseCase listCommercesUseCase(
            CommerceRepositoryPort commerceRepositoryPort) {
        return new ListCommercesUseCase(commerceRepositoryPort);
    }

    @Bean
    public ChangeCommerceStatusUseCase changeCommerceStatusUseCase(
            CommerceRepositoryPort commerceRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new ChangeCommerceStatusUseCase(
                commerceRepositoryPort, productRepositoryPort, userService);
    }

    /// Criterio de búsqueda único para Manage products y el catálogo del cliente
    @Bean
    public TextMatchStrategy textMatchStrategy() {
        return new AccentInsensitiveStrategy();
    }
}