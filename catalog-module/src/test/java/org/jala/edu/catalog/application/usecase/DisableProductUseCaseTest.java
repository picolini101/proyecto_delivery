
package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.port.in.DisableProductCommand;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DisableProductUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();
    private final DisableProductUseCase disableProduct = new DisableProductUseCase(
            productRepository, new FallbackUserService());

    private Commerce commerce;

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Pizzeria"));
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void disablesAnExistingProductWhenRequesterIsAuthorized(UUID requesterId) {
        Product pizza = saveProduct("Pizza", "Comida", 10);

        disableProduct.execute(commandFor(requesterId, pizza));

        Product stored = productRepository.findById(pizza.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(ProductStatus.DISABLED);
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsUnauthorizedUsersAndKeepsTheProductUnchanged(UUID requesterId) {
        Product pizza = saveProduct("Pizza", "Comida", 10);

        Throwable thrown = catchThrowable(
                () -> disableProduct.execute(commandFor(requesterId, pizza)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(productRepository.findById(pizza.getId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.AVAILABLE);
    }

    @Test
    void reportsWhenTheProductDoesNotExist() {
        Throwable thrown = catchThrowable(() -> disableProduct.execute(
                new DisableProductCommand(SampleUsers.ADMIN, UUID.randomUUID())));

        assertThat(thrown).isInstanceOf(ProductNotFoundException.class)
                .hasMessageStartingWith("Product not found");
        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    void preservesProductDataAndStockWhenDisabled() {
        Product pizza = saveProduct("Pizza", "Comida", 10);

        disableProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        Product stored = productRepository.findById(pizza.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Pizza");
        assertThat(stored.getCategory()).isEqualTo("Comida");
        assertThat(stored.getPrice()).isEqualByComparingTo("50.00");
        assertThat(stored.getStock()).isEqualTo(10);
        assertThat(stored.getStatus()).isEqualTo(ProductStatus.DISABLED);
    }

    private Product saveProduct(String name, String category, int stock) {
        Product product = Product.register(
                commerce.getId().value(), name, category, new BigDecimal("50.00"));
        product.adjustStock(stock);
        return productRepository.save(product);
    }

    private static DisableProductCommand commandFor(UUID requesterId, Product product) {
        return new DisableProductCommand(requesterId, product.getId().value());
    }
}
