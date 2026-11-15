package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.DeleteProductResult;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceStatus;
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

class DeleteProductUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();
    private final DeleteProductUseCase deleteProduct = new DeleteProductUseCase(
            productRepository, commerceRepository, new FallbackUserService());

    private Commerce commerce;

    static Stream<UUID> nonAdminUsers() {
        return Stream.of(SampleUsers.MERCHANT, SampleUsers.CUSTOMER,
                SampleUsers.COURIER, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Pizzeria"));
    }

    @Test
    void deletesAnExistingProductWhenRequesterIsAdmin() {
        Product pizza = saveProduct("Pizza", "Comida", 5);
        saveProduct("Pasta", "Comida", 5);

        DeleteProductResult result = deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(result).isEqualTo(DeleteProductResult.DELETED);
        assertThat(productRepository.findById(pizza.getId())).isEmpty();
    }

    @Test
    void removesTheStockTogetherWithTheProduct() {
        Product pizza = saveProduct("Pizza", "Comida", 10);
        saveProduct("Pasta", "Comida", 5);

        deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(productRepository.existsById(pizza.getId())).isFalse();
        assertThat(productRepository.findByCommerceId(commerce.getId().value()))
                .noneMatch(product -> product.getId().equals(pizza.getId()));
    }

    @Test
    void keepsTheOtherProductsUnchanged() {
        Product pizza = saveProduct("Pizza", "Comida", 5);
        Product pasta = saveProduct("Pasta", "Comida", 7);

        deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        Product stored = productRepository.findById(pasta.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Pasta");
        assertThat(stored.getCategory()).isEqualTo("Comida");
        assertThat(stored.getStock()).isEqualTo(7);
        assertThat(productRepository.findAll()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("nonAdminUsers")
    void rejectsUsersWhoAreNotAdminAndKeepsTheProduct(UUID requesterId) {
        Product pizza = saveProduct("Pizza", "Comida", 5);

        Throwable thrown = catchThrowable(() -> deleteProduct.execute(commandFor(requesterId, pizza)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(productRepository.existsById(pizza.getId())).isTrue();
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void reportsWhenTheProductDoesNotExistAndChangesNothing() {
        Product pizza = saveProduct("Pizza", "Comida", 5);

        Throwable thrown = catchThrowable(() -> deleteProduct.execute(
                new DeleteProductCommand(SampleUsers.ADMIN, UUID.randomUUID())));

        assertThat(thrown).isInstanceOf(ProductNotFoundException.class)
                .hasMessageStartingWith("Product not found");
        assertThat(productRepository.findAll()).containsExactly(pizza);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void disablesTheCommerceWhenTheOnlyAvailableProductIsDeleted() {
        Product pizza = saveProduct("Pizza", "Comida", 5);

        DeleteProductResult result = deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(result).isEqualTo(DeleteProductResult.DELETED_COMMERCE_DISABLED);
        assertThat(commerceRepository.findById(commerce.getId()).orElseThrow().getStatus())
                .isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void disablesTheCommerceWhenTheRemainingProductsHaveNoStock() {
        Product pizza = saveProduct("Pizza", "Comida", 5);
        saveProduct("Pasta", "Comida", 0);

        DeleteProductResult result = deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(result).isEqualTo(DeleteProductResult.DELETED_COMMERCE_DISABLED);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void keepsTheCommerceActiveWhenAnotherAvailableProductRemains() {
        Product pizza = saveProduct("Pizza", "Comida", 5);
        saveProduct("Pasta", "Comida", 3);

        deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void keepsTheCommerceActiveWhenTheDeletedProductWasNotAvailable() {
        saveProduct("Pasta", "Comida", 3);
        Product outOfStock = saveProduct("Pizza", "Comida", 0);
        outOfStock.updateStatus(ProductStatus.OUT_OF_STOCK);
        productRepository.save(outOfStock);

        DeleteProductResult result = deleteProduct.execute(commandFor(SampleUsers.ADMIN, outOfStock));

        assertThat(result).isEqualTo(DeleteProductResult.DELETED);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void doesNotReportAChangeWhenTheCommerceWasAlreadyDisabled() {
        commerce.disable();
        commerceRepository.save(commerce);
        Product pizza = saveProduct("Pizza", "Comida", 5);

        DeleteProductResult result = deleteProduct.execute(commandFor(SampleUsers.ADMIN, pizza));

        assertThat(result).isEqualTo(DeleteProductResult.DELETED);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    private Product saveProduct(String name, String category, int stock) {
        Product product = Product.register(
                commerce.getId().value(), name, category, new BigDecimal("50.00"));
        product.adjustStock(stock);
        return productRepository.save(product);
    }

    private static DeleteProductCommand commandFor(UUID requesterId, Product product) {
        return new DeleteProductCommand(requesterId, product.getId().value());
    }
}