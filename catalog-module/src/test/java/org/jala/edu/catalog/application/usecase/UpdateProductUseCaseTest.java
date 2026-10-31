package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.UpdateProductCommand;
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

class UpdateProductUseCaseTest {

    private final InMemoryProductRepositoryAdapter repository = new InMemoryProductRepositoryAdapter();
    private final UpdateProductUseCase updateProduct =
            new UpdateProductUseCase(repository, new FallbackUserService());

    private Product product;

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        // Producto existente sobre el que se prueban las modificaciones
        product = repository.save(Product.register(
                FallbackCatalogService.SAMPLE_COMMERCE, "Pizza", "Food", new BigDecimal("50.00")));
    }

    private UpdateProductCommand commandFor(UUID requesterId, UUID productId, BigDecimal price) {
        return new UpdateProductCommand(requesterId, productId, "Pasta", "Italian", price);
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void savesTheChangesWhenTheUserIsAuthorized(UUID requesterId) {
        // Act
        updateProduct.execute(commandFor(requesterId, product.getId().value(), new BigDecimal("30.00")));

        // Assert
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Pasta");
        assertThat(stored.getCategory()).isEqualTo("Italian");
        assertThat(stored.getPrice()).isEqualByComparingTo("30.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5"})
    void rejectsAPriceNotGreaterThanZeroAndKeepsTheProduct(String price) {
        // Act
        Throwable thrown = catchThrowable(() -> updateProduct.execute(
                commandFor(SampleUsers.MERCHANT, product.getId().value(), new BigDecimal(price))));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Price must be greater than zero");
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getName()).isEqualTo("Pizza");
        assertThat(stored.getPrice()).isEqualByComparingTo("50.00");
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsAUserWithoutPermissionAndKeepsTheProduct(UUID requesterId) {
        // Act
        Throwable thrown = catchThrowable(() -> updateProduct.execute(
                commandFor(requesterId, product.getId().value(), new BigDecimal("30.00"))));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(repository.findById(product.getId()).orElseThrow().getName()).isEqualTo("Pizza");
    }

    @Test
    void reportsAMissingProductAndStoresNothing() {
        // Act
        Throwable thrown = catchThrowable(() -> updateProduct.execute(
                commandFor(SampleUsers.MERCHANT, UUID.randomUUID(), new BigDecimal("30.00"))));

        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException.class)
                .hasMessageStartingWith("Product not found");
        assertThat(repository.findAll()).hasSize(1);
    }
}