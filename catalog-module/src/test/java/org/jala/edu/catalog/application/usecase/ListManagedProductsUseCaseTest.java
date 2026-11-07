package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListManagedProductsUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();

    private final ListManagedProductsUseCase listProducts =
            new ListManagedProductsUseCase(productRepository, commerceRepository);

    @Test
    void returnsEveryProductOldestFirst() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Test Commerce"));

        Product first = productRepository.save(Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00")));
        Product second = productRepository.save(Product.register(
                commerce.getId().value(), "Soda", "Drinks", new BigDecimal("5.00")));

        first.adjustStock(10);
        second.adjustStock(5);
        productRepository.save(first);
        productRepository.save(second);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products)
                .extracting(managed -> managed.product().getName())
                .containsExactly("Pizza", "Soda");
    }

    @Test
    void marksProductAsAvailableWhenCommerceAndProductAreActiveAndStockIsPositive() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        Product product = Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);
        productRepository.save(product);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products).extracting(ManagedProduct::availability)
                .containsExactly("Disponible");
    }

    @Test
    void marksProductAsUnavailableWhenCommerceIsDisabled() {
        // Un comercio recién registrado ya nace Deshabilitado
        Commerce commerce = commerceRepository.save(CatalogFixtures.disabledCommerce("Disabled Commerce"));

        Product product = Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);
        productRepository.save(product);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products).extracting(ManagedProduct::availability)
                .containsExactly("No disponible");
    }

    @Test
    void marksDisabledProductAsUnavailable() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        Product product = Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);
        product.disable();
        productRepository.save(product);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products).extracting(ManagedProduct::availability)
                .containsExactly("No disponible");
    }

    @Test
    void marksProductWithZeroStockAsOutOfStock() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        Product product = Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        productRepository.save(product);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products).extracting(ManagedProduct::availability)
                .containsExactly("Agotado");
    }

    @Test
    void marksProductAsOutOfStockWhenStatusIsOutOfStockEvenWithPositiveStock() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        Product product = Product.register(
                commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));

        product.adjustStock(10);
        product.updateStatus(ProductStatus.OUT_OF_STOCK);
        productRepository.save(product);

        var products = listProducts.execute(new ListManagedProductsQuery());

        assertThat(products).extracting(ManagedProduct::availability)
                .containsExactly("Agotado");
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoProducts() {
        assertThat(listProducts.execute(new ListManagedProductsQuery())).isEmpty();
    }
}