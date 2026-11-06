package org.jala.edu.catalog.infrastructure.config;

import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.application.usecase.CreateProductUseCase;
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
}