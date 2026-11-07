package org.jala.edu.catalog.infrastructure.adapter.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogServiceAdapterTest {

    private final InMemoryProductRepositoryAdapter repository =
            new InMemoryProductRepositoryAdapter();

    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();


    private final CatalogServiceAdapter catalogService =
            new CatalogServiceAdapter(repository, commerceRepository);
    private Product product;

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
}