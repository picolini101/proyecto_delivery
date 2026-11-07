package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListAvailableProductsUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();

    private final ListAvailableProductsUseCase useCase =
            new ListAvailableProductsUseCase(productRepository, commerceRepository);

    private Commerce commerce;

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Test Commerce"));
    }

    //  comercio con productos disponibles -> se muestran
    @Test
    void showsTheAvailableProductsOfTheCommerce() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 3);

        List<CatalogProduct> result = query(commerce);

        assertThat(result).extracting(CatalogProduct::name).containsExactly("Pizza", "Soda");
        assertThat(result).allMatch(CatalogProduct::available);
    }

    //  cada producto expone nombre, categoria y precio
    @Test
    void exposesNameCategoryAndPriceOfEachProduct() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);

        CatalogProduct product = query(commerce).getFirst();

        assertThat(product.name()).isEqualTo("Pizza");
        assertThat(product.category()).isEqualTo("Food");
        assertThat(product.price()).isEqualByComparingTo("50.00");
    }

    //  precio vigente (refleja la ultima actualizacion)
    @Test
    void showsTheCurrentPriceAfterAnUpdate() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.update("Pizza", "Food", new BigDecimal("65.00"));
        productRepository.save(product);

        assertThat(query(commerce).getFirst().price()).isEqualByComparingTo("65.00");
    }

    //  los agotados se muestran como agotados, no como disponibles
    @Test
    void showsAProductWithZeroStockAsSoldOut() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 0);

        List<CatalogProduct> result = query(commerce);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().available()).isFalse();
    }

    @Test
    void showsAProductMarkedOutOfStockAsSoldOutEvenWithStock() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.updateStatus(ProductStatus.OUT_OF_STOCK);
        productRepository.save(product);

        List<CatalogProduct> result = query(commerce);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().available()).isFalse();
    }

    @Test
    void keepsAvailableAndSoldOutProductsApart() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 0);

        assertThat(query(commerce))
                .extracting(CatalogProduct::name, CatalogProduct::available)
                .containsExactly(tuple("Pizza", true), tuple("Soda", false));
    }

    @Test
    void hidesDisabledProducts() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.disable();
        productRepository.save(product);

        assertThat(query(commerce)).isEmpty();
    }

    @Test
    void onlyShowsProductsOfTheRequestedCommerce() {
        Commerce other = commerceRepository.save(CatalogFixtures.activeCommerce("Other Commerce"));
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(other, "Burger", "Food", "30.00", 10);

        assertThat(query(commerce)).extracting(CatalogProduct::name).containsExactly("Pizza");
    }

    @Test
    void showsNothingWhenTheCommerceIsDisabled() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        commerce.disable();
        commerceRepository.save(commerce);

        assertThat(query(commerce)).isEmpty();
    }

    @Test
    void showsNothingWhenTheCommerceDoesNotExist() {
        assertThat(useCase.execute(new ListAvailableProductsQuery(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void returnsAnEmptyListWhenTheCommerceHasNoProducts() {
        assertThat(query(commerce)).isEmpty();
    }

    private List<CatalogProduct> query(Commerce target) {
        return useCase.execute(new ListAvailableProductsQuery(target.getId().value()));
    }

    private Product saveProduct(Commerce owner, String name, String category, String price, int stock) {
        Product product = Product.register(owner.getId().value(), name, category, new BigDecimal(price));
        product.adjustStock(stock);
        return productRepository.save(product);
    }
}