package org.jala.edu.core.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

    @Test
    void anImplementationWithoutTheListingReturnsNoProducts() {
        // Arrange: an implementation written before findAvailableProducts existed
        CatalogService service = new CatalogService() {
            @Override
            public Optional<ProductInfo> findProduct(UUID productId) {
                return Optional.empty();
            }

            @Override
            public boolean isAvailable(UUID productId, int quantity) {
                return false;
            }

            @Override
            public void reserveStock(UUID productId, int quantity) {
            }

            @Override
            public void releaseStock(UUID productId, int quantity) {
            }

            @Override
            public List<UUID> findCommerceIdsByMerchant(UUID merchantId) {
                return List.of();
            }
        };

        // Act
        List<ProductInfo> available = service.findAvailableProducts();

        // Assert
        assertThat(available).isEmpty();
    }
}
