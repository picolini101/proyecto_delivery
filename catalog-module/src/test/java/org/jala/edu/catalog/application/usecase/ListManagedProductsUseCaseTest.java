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
import static org.assertj.core.groups.Tuple.tuple;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
    private Product saveProduct(Commerce commerce, String name, String category) {
        Product product = Product.register(
                commerce.getId().value(), name, category, new BigDecimal("10.00"));
        product.adjustStock(10);
        return productRepository.save(product);
    }

    private static List<String> names(List<ManagedProduct> products) {
        return products.stream().map(managed -> managed.product().getName()).toList();
    }

    @Test
    void returnsOnlyProductsWhoseNameOrCategoryMatchTheSearchText() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        saveProduct(commerce, "Pizza", "Food");
        saveProduct(commerce, "Soda", "Drinks");
        saveProduct(commerce, "Hamburguesa", "Food");

        assertThat(names(listProducts.execute(new ListManagedProductsQuery("piz"))))
                .containsExactly("Pizza");
        assertThat(names(listProducts.execute(new ListManagedProductsQuery("  FOOD "))))
                .containsExactlyInAnyOrder("Pizza", "Hamburguesa");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void returnsEveryProductWhenTheSearchTextIsBlank(String searchText) {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        saveProduct(commerce, "Pizza", "Food");
        saveProduct(commerce, "Soda", "Drinks");

        assertThat(names(listProducts.execute(new ListManagedProductsQuery(searchText))))
                .containsExactlyInAnyOrder("Pizza", "Soda");
    }

    @Test
    void returnsAnEmptyListWhenNothingMatchesTheSearchText() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        saveProduct(commerce, "Pizza", "Food");

        assertThat(listProducts.execute(new ListManagedProductsQuery("xyz"))).isEmpty();
    }

    @Test
    void showsDisabledAndOutOfStockProductsThatMatchTheSearchText() {
        Commerce commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Active Commerce"));
        Product disabled = saveProduct(commerce, "Pizza Fria", "Food");
        disabled.disable();
        productRepository.save(disabled);
        Product soldOut = saveProduct(commerce, "Pizza Grande", "Food");
        soldOut.adjustStock(0);
        productRepository.save(soldOut);

        var result = listProducts.execute(new ListManagedProductsQuery("pizza"));

        assertThat(result)
                .extracting(managed -> managed.product().getName(), ManagedProduct::availability)
                .containsExactlyInAnyOrder(
                        tuple("Pizza Fria", "No disponible"),
                        tuple("Pizza Grande", "Agotado"));
    }
}