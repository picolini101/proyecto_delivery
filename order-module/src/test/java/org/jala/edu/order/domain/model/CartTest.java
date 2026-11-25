package org.jala.edu.order.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import java.math.BigDecimal;
import org.jala.edu.order.domain.exception.ProductNotFoundInCartException;
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

    /** El precio que el cliente vio queda guardado en la linea. */
    @Test
    void remembersThePriceTheCustomerSaw() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2, new BigDecimal("50.00"));
        assertEquals(0, new BigDecimal("50.00").compareTo(cart.findItem(product).getUnitPrice()));
    }

    /** Sin precio, la linea queda sin precio guardado (asi funciona el metodo de tres parametros). */
    @Test
    void leavesThePriceEmptyWhenNoneIsGiven() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2);
        assertNull(cart.findItem(product).getUnitPrice());
    }

    /** Al agregar mas del mismo producto, la linea conserva el precio que el cliente ya vio. */
    @Test
    void keepsTheOriginalPriceWhenAddingMoreOfTheSameProduct() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2, new BigDecimal("50.00"));
        cart.addProduct(product, commerce, 1, new BigDecimal("65.00"));
        assertEquals(3, cart.quantityOf(product));
        assertEquals(0, new BigDecimal("50.00").compareTo(cart.findItem(product).getUnitPrice()));
    }

    /** confirmPrice reemplaza el precio de la linea por el que el cliente acepto. */
    @Test
    void confirmPriceReplacesThePriceOfTheLine() {
        Cart cart = new Cart(UUID.randomUUID());
        cart.addProduct(product, commerce, 2, new BigDecimal("50.00"));
        cart.confirmPrice(product, new BigDecimal("65.00"));
        assertEquals(0, new BigDecimal("65.00").compareTo(cart.findItem(product).getUnitPrice()));
        assertEquals(2, cart.quantityOf(product));
    }

    /** confirmPrice de un producto que no esta en el carrito lanza ProductNotFoundInCartException. */
    @Test
    void confirmPriceFailsWhenTheProductIsNotInTheCart() {
        Cart cart = new Cart(UUID.randomUUID());
        assertThrows(ProductNotFoundInCartException.class, () -> cart.confirmPrice(product, new BigDecimal("65.00")));
    }

}