package org.jala.edu.core.infrastructure.contract;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;

/**
 * Stand-in {@link CatalogService} with the {@link SampleCatalog}: one commerce owned by {@link SampleUsers#MERCHANT},
 * an available product with stock and a sold-out one. Stock changes are kept in memory and lost when the application
 * stops.
 */
public class FallbackCatalogService implements CatalogService {

    /** @deprecated use {@link SampleCatalog#COMMERCE}, which {@code catalog-module} also stores (#87). */
    @Deprecated(forRemoval = true)
    public static final UUID SAMPLE_COMMERCE = SampleCatalog.COMMERCE;
    /** @deprecated use {@link SampleCatalog#PIZZA}, which {@code catalog-module} also stores (#87). */
    @Deprecated(forRemoval = true)
    public static final UUID AVAILABLE_PRODUCT = SampleCatalog.PIZZA;
    /** @deprecated use {@link SampleCatalog#EMPANADA}, which {@code catalog-module} also stores (#87). */
    @Deprecated(forRemoval = true)
    public static final UUID SOLD_OUT_PRODUCT = SampleCatalog.EMPANADA;

    private static final Map<UUID, ProductInfo> PRODUCTS = Map.of(
            SampleCatalog.PIZZA, new ProductInfo(SampleCatalog.PIZZA, SampleCatalog.COMMERCE, SampleCatalog.PIZZA_NAME,
                    SampleCatalog.PIZZA_PRICE),
            SampleCatalog.EMPANADA, new ProductInfo(SampleCatalog.EMPANADA, SampleCatalog.COMMERCE,
                    SampleCatalog.EMPANADA_NAME, SampleCatalog.EMPANADA_PRICE));

    private final Map<UUID, Integer> stock =
            new ConcurrentHashMap<>(Map.of(SampleCatalog.PIZZA, SampleCatalog.PIZZA_STOCK));

    @Override
    public Optional<ProductInfo> findProduct(UUID productId) {
        return Optional.ofNullable(PRODUCTS.get(productId));
    }

    @Override
    public boolean isAvailable(UUID productId, int quantity) {
        return quantity > 0 && stock.getOrDefault(productId, 0) >= quantity;
    }

    @Override
    public void reserveStock(UUID productId, int quantity) {
        requireProduct(productId);
        if (!isAvailable(productId, quantity)) {
            throw new ConflictException("Not enough stock for product " + productId);
        }
        stock.merge(productId, -quantity, Integer::sum);
    }

    @Override
    public void releaseStock(UUID productId, int quantity) {
        requireProduct(productId);
        stock.merge(productId, quantity, Integer::sum);
    }

    @Override
    public List<UUID> findCommerceIdsByMerchant(UUID merchantId) {
        return SampleUsers.MERCHANT.equals(merchantId) ? List.of(SampleCatalog.COMMERCE) : List.of();
    }

    private void requireProduct(UUID productId) {
        if (!PRODUCTS.containsKey(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
    }
}
