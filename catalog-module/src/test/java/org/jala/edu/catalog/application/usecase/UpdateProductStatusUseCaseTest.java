package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.UpdateProductStatusCommand;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UpdateProductStatusUseCaseTest {

    private final InMemoryProductRepositoryAdapter repository =
            new InMemoryProductRepositoryAdapter();

    private final UpdateProductStatusUseCase updateProductStatus =
            new UpdateProductStatusUseCase(repository, new FallbackUserService());

    private Product product;

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(
                SampleUsers.CUSTOMER,
                SampleUsers.COURIER,
                UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        product = repository.save(Product.register(
                FallbackCatalogService.SAMPLE_COMMERCE,
                "Pizza",
                "Food",
                new BigDecimal("50.00")));
        product.adjustStock(10);
        repository.save(product);
    }

    private UpdateProductStatusCommand commandFor(
            UUID requesterId, UUID productId, ProductStatus status) {
        return new UpdateProductStatusCommand(requesterId, productId, status);
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void updatesProductStatusWhenUserIsAuthorized(UUID requesterId) {
        updateProductStatus.execute(commandFor(
                requesterId, product.getId().value(), ProductStatus.OUT_OF_STOCK));

        Product stored = repository.findById(product.getId()).orElseThrow();

        assertThat(stored.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        assertThat(stored.getStock()).isEqualTo(10);
    }

    @Test
    void allowsChangingStatusBackToAvailable() {
        product.updateStatus(ProductStatus.OUT_OF_STOCK);
        repository.save(product);

        updateProductStatus.execute(commandFor(
                SampleUsers.ADMIN, product.getId().value(), ProductStatus.AVAILABLE));

        Product stored = repository.findById(product.getId()).orElseThrow();

        assertThat(stored.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        assertThat(stored.getStock()).isEqualTo(10);
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsUnauthorizedUsersAndKeepsPreviousStatus(UUID requesterId) {
        Throwable thrown = catchThrowable(() -> updateProductStatus.execute(commandFor(
                requesterId, product.getId().value(), ProductStatus.OUT_OF_STOCK)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");

        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(ProductStatus.AVAILABLE);
        assertThat(stored.getStock()).isEqualTo(10);
    }

    @Test
    void rejectsDisabledAsAnInvalidStatus() {
        Throwable thrown = catchThrowable(() -> updateProductStatus.execute(commandFor(
                SampleUsers.ADMIN, product.getId().value(), ProductStatus.DISABLED)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Invalid product status");

        assertThat(repository.findById(product.getId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.AVAILABLE);
    }

    @Test
    void rejectsNullAsAnInvalidStatus() {
        Throwable thrown = catchThrowable(() -> updateProductStatus.execute(commandFor(
                SampleUsers.ADMIN, product.getId().value(), null)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Invalid product status");
    }

    @Test
    void reportsWhenProductDoesNotExist() {
        Throwable thrown = catchThrowable(() -> updateProductStatus.execute(commandFor(
                SampleUsers.ADMIN, UUID.randomUUID(), ProductStatus.OUT_OF_STOCK)));

        assertThat(thrown).isInstanceOf(ProductNotFoundException.class)
                .hasMessageStartingWith("Product not found");

        assertThat(repository.findAll()).hasSize(1);
    }
}
