package org.jala.edu.order.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class CartTest {

    private final UUID product = UUID.randomUUID();
    private final UUID commerce = UUID.randomUUID();

    @Test
    void addsNewProduct() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2);
        assertEquals(2, cart.quantityOf(product));
    }

    @Test
    void sumsQuantityWhenProductAlreadyInCart() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2);
        cart.addProduct(product, commerce, 3);
        assertEquals(5, cart.quantityOf(product));
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        Cart cart = new Cart(UUID.randomUUID());
        assertThrows(DomainException.class, () -> cart.addProduct(product, commerce, 0));
    }
}
