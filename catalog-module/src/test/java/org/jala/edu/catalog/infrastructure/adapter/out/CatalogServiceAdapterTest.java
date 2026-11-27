package org.jala.edu.catalog.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.usecase.ListAvailableProductsUseCase;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.CommerceInfo;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.application.usecase.DeleteProductUseCase;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;

class CatalogServiceAdapterTest {

    private final InMemoryProductRepositoryAdapter repository =
            new InMemoryProductRepositoryAdapter();

    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();

    private Product product;

    private final CatalogServiceAdapter catalogService =
            new CatalogServiceAdapter(
                    repository,
                    commerceRepository,
                    new ListAvailableProductsUseCase(repository, commerceRepository, 10));

    @BeforeEach
    void setUp() {
        Commerce commerce = Commerce.restore(
                new CommerceId(SampleCatalog.COMMERCE),
                "My Commerce",
                CatalogFixtures.ADDRESS,
                CatalogFixtures.PHONE,
                SampleUsers.MERCHANT,
                CommerceStatus.ACTIVE,
                Instant.now(),
                Instant.now());

        commerceRepository.save(commerce);

        product = Product.register(
                commerce.getId().value(),
                "Pizza",
                "Food",
                new BigDecimal("50.00"));
        product.adjustStock(10);
        repository.save(product);
    }

    @Test
    void findsAProductById() {
        // Act
        ProductInfo result = catalogService.findProduct(product.getId().value()).orElseThrow();

        // Assert
        assertThat(result.productId()).isEqualTo(product.getId().value());
        assertThat(result.commerceId()).isEqualTo(product.getCommerceId());
        assertThat(result.name()).isEqualTo("Pizza");
        assertThat(result.price()).isEqualByComparingTo("50.00");
    }

    @Test
    void returnsEmptyWhenTheProductDoesNotExist() {
        // Act
        var result = catalogService.findProduct(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void availabilityDependsOnTheAvailableStock() {
        // Act
        boolean enoughStock = catalogService.isAvailable(product.getId().value(), 10);
        boolean insufficientStock = catalogService.isAvailable(product.getId().value(), 11);
        boolean invalidQuantity = catalogService.isAvailable(product.getId().value(), 0);

        // Assert
        assertThat(enoughStock).isTrue();
        assertThat(insufficientStock).isFalse();
        assertThat(invalidQuantity).isFalse();
    }

    @Test
    void reservingStockDecreasesTheStoredQuantity() {
        // Act
        catalogService.reserveStock(product.getId().value(), 4);

        // Assert
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStock()).isEqualTo(6);
    }

    @Test
    void releasingStockIncreasesTheStoredQuantity() {
        // Arrange
        catalogService.reserveStock(product.getId().value(), 4);

        // Act
        catalogService.releaseStock(product.getId().value(), 4);

        // Assert
        Product stored = repository.findById(product.getId()).orElseThrow();
        assertThat(stored.getStock()).isEqualTo(10);
    }

    @Test
    void cannotReserveMoreThanTheAvailableStock() {
        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.reserveStock(product.getId().value(), 11));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(repository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void cannotReserveAnInvalidQuantity() {
        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.reserveStock(product.getId().value(), 0));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(repository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void cannotReleaseAnInvalidQuantity() {
        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.releaseStock(product.getId().value(), 0));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(repository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void throwsWhenReservingStockForAnUnknownProduct() {
        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.reserveStock(UUID.randomUUID(), 1));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @Test
    void throwsWhenReleasingStockForAnUnknownProduct() {
        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.releaseStock(UUID.randomUUID(), 1));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @Test
    void productIsUnavailableWhenCommerceIsDisabled() {
        Commerce commerce = commerceRepository.findById(
                new CommerceId(product.getCommerceId())).orElseThrow();

        commerce.disable();
        commerceRepository.save(commerce);

        assertThat(catalogService.isAvailable(product.getId().value(), 1)).isFalse();
    }

    @Test
    void productIsUnavailableWhenProductIsDisabled() {
        product.disable();

        assertThat(catalogService.isAvailable(product.getId().value(), 1)).isFalse();
    }

    @Test
    void cannotReserveStockForADisabledProduct() {
        // Arrange
        product.disable();

        // Act
        Throwable thrown = catchThrowable(
                () -> catalogService.reserveStock(product.getId().value(), 1));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(repository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }

    @Test
    void productIsUnavailableWhenStockIsZero() {
        product.adjustStock(0);

        assertThat(catalogService.isAvailable(product.getId().value(), 1)).isFalse();
    }

    @Test
    void productIsUnavailableWhenStatusIsOutOfStock() {
        product.updateStatus(ProductStatus.OUT_OF_STOCK);

        assertThat(catalogService.isAvailable(product.getId().value(), 1)).isFalse();
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void findsTheCommercesOwnedByAMerchant() {
        assertThat(catalogService.findCommerceIdsByMerchant(SampleUsers.MERCHANT))
                .containsExactly(SampleCatalog.COMMERCE);
        assertThat(catalogService.findCommerceIdsByMerchant(UUID.randomUUID())).isEmpty();
    }


    @Test
    void listsAnAvailableProductWithItsCommerceNameAndPrice() {
        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).hasSize(1);
        ProductInfo listed = result.get(0);
        assertThat(listed.productId()).isEqualTo(product.getId().value());
        assertThat(listed.commerceId()).isEqualTo(SampleCatalog.COMMERCE);
        assertThat(listed.name()).isEqualTo("Pizza");
        assertThat(listed.price()).isEqualByComparingTo("50.00");
    }

    @Test
    void doesNotListASoldOutProduct() {
        // Arrange
        product.adjustStock(0);
        repository.save(product);

        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void doesNotListADisabledProduct() {
        // Arrange
        product.disable();
        repository.save(product);

        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void doesNotListTheProductsOfAnInactiveCommerce() {
        // Arrange
        Commerce commerce = commerceRepository.findById(
                new CommerceId(product.getCommerceId())).orElseThrow();
        commerce.disable();
        commerceRepository.save(commerce);

        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void listsEveryProductEvenWhenTheyDoNotFitInOnePage() {
        // Arrange: the page size is 10 and there is already 1 product (Pizza)
        for (int i = 1; i <= 14; i++) {
            Product extra = Product.register(
                    SampleCatalog.COMMERCE, "Product " + i, "Food", new BigDecimal("10.00"));
            extra.adjustStock(5);
            repository.save(extra);
        }

        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).hasSize(15);
        assertThat(result).extracting(ProductInfo::productId).doesNotHaveDuplicates();
    }

    @Test
    void keepsEachProductWithItsOwnCommerce() {
        // Arrange
        Commerce other = CatalogFixtures.activeCommerce("Other Commerce");
        commerceRepository.save(other);
        Product burger = Product.register(
                other.getId().value(), "Burger", "Food", new BigDecimal("30.00"));
        burger.adjustStock(3);
        repository.save(burger);

        // Act
        List<ProductInfo> result = catalogService.findAvailableProducts();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).filteredOn(p -> p.name().equals("Pizza"))
                .extracting(ProductInfo::commerceId).containsExactly(SampleCatalog.COMMERCE);
        assertThat(result).filteredOn(p -> p.name().equals("Burger"))
                .extracting(ProductInfo::commerceId).containsExactly(other.getId().value());
    }
    @Test
    void doesNotReturnAProductDeletedByTheAdministrator() {
        // Arrange: se elimina el producto con el caso de uso real
        new DeleteProductUseCase(repository, commerceRepository, new FallbackUserService())
                .execute(new DeleteProductCommand(SampleUsers.ADMIN, product.getId().value()));

        // Assert: otros módulos lo ven como inexistente / no disponible
        assertThat(catalogService.findProduct(product.getId().value())).isEmpty();
        assertThat(catalogService.isAvailable(product.getId().value(), 1)).isFalse();
        assertThat(catalogService.findAvailableProducts()).isEmpty();
    }

    @Test
    void doesNotFindAProductWhenItsCommerceDoesNotExist() {
        commerceRepository.deleteById(new CommerceId(product.getCommerceId()));

        assertThat(catalogService.findProduct(product.getId().value())).isEmpty();
    }

    @Test
    void findsACommerceByIdWithItsIdAndName() {
        // Act
        Optional<CommerceInfo> result = catalogService.findCommerce(SampleCatalog.COMMERCE);

        // Assert
        assertThat(result).contains(new CommerceInfo(SampleCatalog.COMMERCE, "My Commerce"));
    }

    @Test
    void findsNoCommerceWhenTheIdDoesNotExist() {
        // Act
        Optional<CommerceInfo> result = catalogService.findCommerce(UUID.randomUUID());

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findsADisabledCommerceToo() {
        // Arrange: an order keeps showing the name of a commerce that was disabled after the sale
        Commerce disabled = CatalogFixtures.disabledCommerce("Closed Diner");
        commerceRepository.save(disabled);

        // Act
        Optional<CommerceInfo> result = catalogService.findCommerce(disabled.getId().value());

        // Assert
        assertThat(result).contains(new CommerceInfo(disabled.getId().value(), "Closed Diner"));
    }

    @Test
    void findsNoCommerceForANullId() {
        // Act
        Optional<CommerceInfo> result = catalogService.findCommerce(null);

        // Assert
        assertThat(result).isEmpty();
    }
}