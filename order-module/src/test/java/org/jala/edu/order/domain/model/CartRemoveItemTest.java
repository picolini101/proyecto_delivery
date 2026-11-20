package org.jala.edu.order.domain.model;

import org.jala.edu.order.domain.exception.ProductNotFoundInCartException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartRemoveItemTest {
    private final UUID customerId = UUID.randomUUID();
    private final UUID commerceId = UUID.randomUUID();
    private final UUID productA = UUID.randomUUID();
    private final UUID productB = UUID.randomUUID();
    @Test
    @DisplayName("Escenario 1: Remocion de un producto cuando hay varios deja los demas intactos")
    void shouldRemoveSpecificItemWhenMultipleItemsExist() {
        Cart cart = new Cart(customerId);
        cart.addProduct(productA, commerceId, 3);
        cart.addProduct(productB, commerceId, 2);
        cart.removeItem(productA);
        assertEquals(0, cart.quantityOf(productA));
        assertEquals(2, cart.quantityOf(productB));
        assertEquals(1, cart.getItems().size());
        assertNull(cart.findItem(productA));
        assertNotNull(cart.findItem(productB));
    }
    @Test
    @DisplayName("Escenario 2: Eliminacion del unico producto deja el carrito completamente vacio")
    void shouldLeaveCartEmptyWhenRemovingTheOnlyItem() {
        Cart cart = new Cart(customerId);
        cart.addProduct(productA, commerceId, 5);
        cart.removeItem(productA);
        assertEquals(0, cart.quantityOf(productA));
        assertTrue(cart.getItems().isEmpty());
    }
    @Test
    @DisplayName("Escenario 3: Intentar eliminar un producto inexistente lanza ProductNotFoundInCartException")
    void shouldThrowExceptionWhenProductNotInCart() {
        Cart cart = new Cart(customerId);
        cart.addProduct(productA, commerceId, 1);
        UUID nonExistentProductId = UUID.randomUUID();
        ProductNotFoundInCartException exception = assertThrows(
                ProductNotFoundInCartException.class,
                () -> cart.removeItem(nonExistentProductId)
        );
        assertTrue(exception.getMessage().contains(nonExistentProductId.toString()));
        assertEquals(1, cart.getItems().size(), "El estado del carrito no debio haber sido alterado");
    }
}