package org.jala.edu.order.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.domain.exception.DomainException;

public class Cart {

    public static final BigDecimal SHIPPING_COST = new BigDecimal("10.00");

    private final UUID customerId;
    private final List<CartItem> items = new ArrayList<>();

    public Cart(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getCustomerId() { return customerId; }

    public List<CartItem> getItems() { return List.copyOf(items); }

    public Optional<CartItem> findItem(UUID productId) {
        return items.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
    }

    public int quantityOf(UUID productId) {
        return findItem(productId).map(CartItem::getQuantity).orElse(0);
    }

    public void addProduct(UUID productId, UUID commerceId, int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        findItem(productId).ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + quantity),
                () -> items.add(new CartItem(productId, commerceId, quantity)));
    }
}
