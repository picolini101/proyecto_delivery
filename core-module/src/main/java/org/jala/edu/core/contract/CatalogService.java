package org.jala.edu.core.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Product lookup and stock offered by {@code catalog-module}; {@code order-module} uses it to list the products a
 * customer can add to the cart, to build and confirm orders and to show each commerce its own orders. The stock
 * operations throw {@code NotFoundException} for an unknown product id.
 */
public interface CatalogService {

    /**
     * @return the product's commerce, name and current price, or empty if no product has that id
     */
    Optional<ProductInfo> findProduct(UUID productId);

    /**
     * The products a customer can add to the cart: those available in an active commerce with stock left, as in
     * {@link #isAvailable(UUID, int)} with quantity 1. Each one carries its commerce, name and current price.
     * The default returns no products, so an implementation written before this method keeps compiling until it
     * overrides it.
     *
     * @return the available products, empty if there are none
     */
    default List<ProductInfo> findAvailableProducts() {
        return List.of();
    }

    /**
     * A product is available when it is marked available, its commerce is active and its stock covers the quantity.
     */
    boolean isAvailable(UUID productId, int quantity);

    /**
     * Takes the quantity out of the product's stock when the order is confirmed. Throws {@code ConflictException}
     * if the stock does not cover it.
     */
    void reserveStock(UUID productId, int quantity);

    /**
     * Puts the quantity back into the product's stock when a confirmed order is cancelled.
     */
    void releaseStock(UUID productId, int quantity);

    /**
     * The commerce's id and name, so {@code order-module} can show the commerce of an order without depending on
     * {@code catalog-module}. A disabled commerce is still returned: an order keeps showing the commerce it was
     * bought from. The default returns empty, so an implementation written before this method keeps compiling until
     * it overrides it.
     *
     * @return the commerce, or empty if no commerce has that id or the id is {@code null}
     */
    default Optional<CommerceInfo> findCommerce(UUID commerceId) {
        return Optional.empty();
    }

    /**
     * @return the commerces owned by that merchant, empty if it owns none
     */
    List<UUID> findCommerceIdsByMerchant(UUID merchantId);
}
