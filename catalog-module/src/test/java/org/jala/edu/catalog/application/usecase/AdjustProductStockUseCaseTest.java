
package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.AdjustProductStockCommand;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AdjustProductStockUseCaseTest {

    private final InMemoryProductRepositoryAdapter repository = new InMemoryProductRepositoryAdapter();
    private final AdjustProductStockUseCase adjustProductStock =
            new AdjustProductStockUseCase(repository, new FallbackUserService());

    private Product product;

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        product = repository.save(Product.register(
                FallbackCatalogService.SAMPLE_COMMERCE, "Pizza", "Food", new BigDecimal("50.00")));
    }

    private AdjustProductStockCommand commandFor(UUID requesterId, UUID productId, int quantity) {
        return new AdjustProductStockCommand(requesterId, productId, quantity);
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void savesTheStockWhenTheUserIsAuthorized(UUID requesterId) {
        // Act
        adjustProductStock.execute(commandFor(requesterId, product.getId().value(), 15));

        // Assert
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStock()).isEqualTo(15);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -5})
    void rejectsNegativeStockAndKeepsThePreviousStock(int quantity) {
        // Act
        Throwable thrown = catchThrowable(() -> adjustProductStock.execute(
                commandFor(SampleUsers.MERCHANT, product.getId().value(), quantity)));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Stock cannot be negative");
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStock()).isZero();
    }

    @Test
    void allowsSettingStockToZero() {
        // Arrange
        product.adjustStock(10);
        repository.save(product);

        // Act
        adjustProductStock.execute(commandFor(
                SampleUsers.MERCHANT, product.getId().value(), 0));

        // Assert
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStock()).isZero();
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsAUserWithoutPermissionAndKeepsTheStock(UUID requesterId) {
        // Arrange
        product.adjustStock(10);
        repository.save(product);

        // Act
        Throwable thrown = catchThrowable(() -> adjustProductStock.execute(
                commandFor(requesterId, product.getId().value(), 20)));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(repository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void reportsAMissingProductAndStoresNothing() {
        // Act
        Throwable thrown = catchThrowable(() -> adjustProductStock.execute(
                commandFor(SampleUsers.MERCHANT, UUID.randomUUID(), 15)));

        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class)
                .hasMessageStartingWith("Product not found");
        assertThat(repository.findAll()).hasSize(1);
    }
}
