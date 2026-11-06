package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.junit.jupiter.api.Test;

class ListManagedProductsUseCaseTest {

    private final InMemoryProductRepositoryAdapter repository = new InMemoryProductRepositoryAdapter();
    private final ListManagedProductsUseCase listProducts = new ListManagedProductsUseCase(repository);

    @Test
    void returnsEveryProductOldestFirst() {
        // Arrange
        Product first = repository.save(Product.register(
                FallbackCatalogService.SAMPLE_COMMERCE, "Pizza", "Food", new BigDecimal("50.00")));
        Product second = repository.save(Product.register(
                FallbackCatalogService.SAMPLE_COMMERCE, "Soda", "Drinks", new BigDecimal("5.00")));

        // Act
        var products = listProducts.execute(new ListManagedProductsQuery());

        // Assert
        assertThat(products).containsExactly(first, second);
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoProducts() {
        assertThat(listProducts.execute(new ListManagedProductsQuery())).isEmpty();
    }
}