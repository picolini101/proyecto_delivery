package org.jala.edu.order.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la clase {@link Cart}: reglas basicas de agregar productos al carrito.
 */
class CartTest {

    /** Producto de prueba. */
    private final UUID product = UUID.randomUUID();

    /** Comercio de prueba. */
    private final UUID commerce = UUID.randomUUID();

    /** Un producto nuevo queda en el carrito con la cantidad indicada. */
    @Test
    void addsNewProduct() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2);
        assertEquals(2, cart.quantityOf(product));
    }

    /** Si el producto ya estaba, se suma la cantidad y no se crea una segunda linea (2 + 3 = 5 en una sola linea). */
    @Test
    void sumsQuantityWhenProductAlreadyInCart() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2);
        cart.addProduct(product, commerce, 3);
        assertEquals(5, cart.quantityOf(product));
        assertEquals(1, cart.getItems().size());
    }

    /** Una cantidad de cero lanza DomainException. */
    @Test
    void rejectsZeroOrNegativeQuantity() {
        Cart cart = new Cart(UUID.randomUUID());
        try {
            cart.addProduct(product, commerce, 0);
            fail("Debia lanzar DomainException");
        } catch (DomainException e) {
            // Era lo esperado
        }
    }
}