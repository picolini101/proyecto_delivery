package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.CatalogProductPage;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.domain.exception.PageNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListAvailableProductsUseCaseTest {

    private static final int PAGE_SIZE = 2;

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();

    private final ListAvailableProductsUseCase useCase =
            new ListAvailableProductsUseCase(
                    productRepository, commerceRepository, PAGE_SIZE);

    private CatalogProductPage search(Commerce target, int page, String text) {
        return useCase.execute(
                new ListAvailableProductsQuery(target.getId().value(), page, text));
    }

    private Commerce commerce;

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Test Commerce"));
    }

    @Test
    void showsTheAvailableProductsOfTheCommerce() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 3);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza", "Soda");
        assertThat(result.products()).allMatch(CatalogProduct::available);
    }

    @Test
    void exposesNameCategoryAndPriceOfEachProduct() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);

        CatalogProduct product = query(commerce, 1).products().getFirst();

        assertThat(product.name()).isEqualTo("Pizza");
        assertThat(product.category()).isEqualTo("Food");
        assertThat(product.price()).isEqualByComparingTo("50.00");
    }

    @Test
    void showsTheCurrentPriceAfterAnUpdate() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.update("Pizza", "Food", new BigDecimal("65.00"));
        productRepository.save(product);

        assertThat(query(commerce, 1).products().getFirst().price())
                .isEqualByComparingTo("65.00");
    }

    @Test
    void showsAProductWithZeroStockAsSoldOut() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 0);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().available()).isFalse();
    }

    @Test
    void showsAProductMarkedOutOfStockAsSoldOutEvenWithStock() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.updateStatus(ProductStatus.OUT_OF_STOCK);
        productRepository.save(product);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().available()).isFalse();
    }

    @Test
    void keepsAvailableAndSoldOutProductsApart() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 0);

        assertThat(query(commerce, 1).products())
                .extracting(CatalogProduct::name, CatalogProduct::available)
                .containsExactly(tuple("Pizza", true), tuple("Soda", false));
    }

    @Test
    void hidesDisabledProducts() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.disable();
        productRepository.save(product);

        assertThat(query(commerce, 1).products()).isEmpty();
    }

    @Test
    void onlyShowsProductsOfTheRequestedCommerce() {
        Commerce other = commerceRepository.save(CatalogFixtures.activeCommerce("Other Commerce"));
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(other, "Burger", "Food", "30.00", 10);

        assertThat(query(commerce, 1).products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza");
    }

    @Test
    void showsNothingWhenTheCommerceIsDisabled() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        commerce.disable();
        commerceRepository.save(commerce);

        assertThat(query(commerce, 1).products()).isEmpty();
    }

    @Test
    void showsNothingWhenTheCommerceDoesNotExist() {
        CatalogProductPage result = useCase.execute(
                new ListAvailableProductsQuery(UUID.randomUUID(), 1));

        assertThat(result.products()).isEmpty();
    }

    @Test
    void returnsAnEmptyPageWhenTheCommerceHasNoProducts() {
        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products()).isEmpty();
        assertThat(result.totalProducts()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void returnsTheFirstPageOfProducts() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza", "Soda");
        assertThat(result.currentPage()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(PAGE_SIZE);
        assertThat(result.totalProducts()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.totalAvailableProducts()).isEqualTo(3);
    }

    @Test
    void returnsTheNextPageOfProducts() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        CatalogProductPage result = query(commerce, 2);

        assertThat(result.products())
                .extracting(CatalogProduct::name)
                .containsExactly("Burger");
        assertThat(result.currentPage()).isEqualTo(2);
        assertThat(result.totalAvailableProducts()).isEqualTo(3);
    }

    @Test
    void showsAllProductsWhenTheyFitInOnePage() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products()).hasSize(2);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void doesNotCountDisabledProductsInPagination() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);

        Product disabled = saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        disabled.disable();
        productRepository.save(disabled);

        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        CatalogProductPage result = query(commerce, 1);

        assertThat(result.products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza", "Burger");
        assertThat(result.totalProducts()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void throwsExceptionWhenRequestedPageDoesNotExist() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        assertThatThrownBy(() -> query(commerce, 3))
                .isInstanceOf(PageNotFoundException.class);
    }

    private CatalogProductPage query(Commerce target, int page) {
        return useCase.execute(
                new ListAvailableProductsQuery(target.getId().value(), page));
    }

    private Product saveProduct(
            Commerce owner, String name, String category, String price, int stock) {
        Product product = Product.register(
                owner.getId().value(), name, category, new BigDecimal(price));
        product.adjustStock(stock);
        return productRepository.save(product);
    }

    @Test
    void filtersByProductName() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);

        assertThat(search(commerce, 1, "piz").products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza");
    }

    @Test
    void filtersByCategory() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        assertThat(search(commerce, 1, "food").products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza", "Burger");
    }

    @Test
    void ignoresCaseWhenSearching() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);

        assertThat(search(commerce, 1, "PIZZA").products()).hasSize(1);
    }

    @Test
    void showsTheWholeCatalogWhenTheTextIsBlank() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);

        assertThat(search(commerce, 1, "  ").products()).hasSize(2);
        assertThat(search(commerce, 1, null).products()).hasSize(2);
    }

    @Test
    void returnsAnEmptyPageWhenNothingMatches() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);

        CatalogProductPage result = search(commerce, 1, "sushi");

        assertThat(result.products()).isEmpty();
        assertThat(result.totalProducts()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void filtersTheWholeCatalogBeforePaginating() {
        saveProduct(commerce, "Pizza Margarita", "Food", "50.00", 10);
        saveProduct(commerce, "Pizza Hawaiana", "Food", "55.00", 10);
        saveProduct(commerce, "Pizza Pepperoni", "Food", "60.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Agua", "Drinks", "3.00", 10);

        CatalogProductPage page2 = search(commerce, 2, "pizza");

        assertThat(page2.products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza Pepperoni");
        assertThat(page2.totalProducts()).isEqualTo(3);
        assertThat(page2.totalPages()).isEqualTo(2);
    }

    @Test
    void keepsSoldOutProductsThatMatch() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 0);

        CatalogProductPage result = search(commerce, 1, "pizza");

        assertThat(result.products()).hasSize(1);
        assertThat(result.products().getFirst().available()).isFalse();
    }

    @Test
    void hidesDisabledProductsEvenWhenTheyMatch() {
        Product product = saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        product.disable();
        productRepository.save(product);

        assertThat(search(commerce, 1, "pizza").products()).isEmpty();
    }

    @Test
    void onlySearchesInTheRequestedCommerce() {
        Commerce other = commerceRepository.save(CatalogFixtures.activeCommerce("Other Commerce"));
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(other, "Pizza Napolitana", "Food", "60.00", 10);

        assertThat(search(commerce, 1, "pizza").products())
                .extracting(CatalogProduct::name)
                .containsExactly("Pizza");
    }

    @Test
    void throwsExceptionWhenThePageIsBeyondTheFilteredResults() {
        saveProduct(commerce, "Pizza", "Food", "50.00", 10);
        saveProduct(commerce, "Soda", "Drinks", "5.00", 10);
        saveProduct(commerce, "Burger", "Food", "30.00", 10);

        assertThatThrownBy(() -> search(commerce, 2, "pizza"))
                .isInstanceOf(PageNotFoundException.class);
    }
}