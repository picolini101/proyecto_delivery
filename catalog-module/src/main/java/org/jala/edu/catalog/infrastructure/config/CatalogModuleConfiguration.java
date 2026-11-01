package org.jala.edu.catalog.infrastructure.config;

import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.application.usecase.AdjustProductStockUseCase;
import org.jala.edu.catalog.application.usecase.ChangeCommerceStatusUseCase;
import org.jala.edu.catalog.application.usecase.CreateProductUseCase;
import org.jala.edu.catalog.application.usecase.ListManagedProductsUseCase;
import org.jala.edu.catalog.application.usecase.RegisterCommerceUseCase;
import org.jala.edu.catalog.application.usecase.UpdateProductUseCase;
import org.jala.edu.catalog.application.usecase.UpdateProductStatusUseCase;
import org.jala.edu.core.contract.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatalogModuleConfiguration {

    @Bean
    public CreateProductUseCase createProductUseCase(ProductRepositoryPort productRepositoryPort,
                                                     UserService userService) {
        return new CreateProductUseCase(productRepositoryPort, userService);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepositoryPort productRepositoryPort,
                                                     UserService userService) {
        return new UpdateProductUseCase(productRepositoryPort, userService);
    }

    @Bean
    public ListManagedProductsUseCase listManagedProductsUseCase(
            ProductRepositoryPort productRepositoryPort,
            CommerceRepositoryPort commerceRepositoryPort) {
        return new ListManagedProductsUseCase(productRepositoryPort, commerceRepositoryPort);
    }

    @Bean
    public AdjustProductStockUseCase adjustProductStockUseCase(
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new AdjustProductStockUseCase(productRepositoryPort, userService);
    }

    @Bean
    public RegisterCommerceUseCase registerCommerceUseCase(
            CommerceRepositoryPort commerceRepositoryPort,
            UserService userService) {
        return new RegisterCommerceUseCase(commerceRepositoryPort, userService);
    }

    @Bean
    public ChangeCommerceStatusUseCase changeCommerceStatusUseCase(
            CommerceRepositoryPort commerceRepositoryPort,
            UserService userService) {
        return new ChangeCommerceStatusUseCase(commerceRepositoryPort, userService);
    }

    @Bean
    public UpdateProductStatusUseCase updateProductStatusUseCase(
            ProductRepositoryPort productRepositoryPort,
            UserService userService) {
        return new UpdateProductStatusUseCase(productRepositoryPort, userService);
    }
}
