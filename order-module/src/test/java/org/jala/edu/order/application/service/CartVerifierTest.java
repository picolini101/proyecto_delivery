package org.jala.edu.order.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.order.domain.exception.ProductUnavailableException;
import org.jala.edu.order.domain.model.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link CartVerifier} (HU6).
 * Usa el catalogo de prueba del core: el producto AVAILABLE tiene stock 10 y el producto SOLD_OUT no tiene stock.
 * Un producto con un id cualquiera (UUID.randomUUID()) no existe en el catalogo.
 */
class CartVerifierTest {

    /** Producto de prueba con stock 10. */
    private static final UUID PRODUCT = FallbackCatalogService.AVAILABLE_PRODUCT;

    /** Producto de prueba agotado. */
    private static final UUID SOLD_OUT = FallbackCatalogService.SOLD_OUT_PRODUCT;

    /** Comercio de prueba (el verificador no lo usa, pero el carrito lo pide). */
    private static final UUID COMMERCE = UUID.randomUUID();

    /** Catalogo de prueba: tambien sirve para leer el precio y el nombre que se esperan. */
    private FallbackCatalogService catalog;

    /** Clase que se esta probando. */
    private CartVerifier verifier;

    /** Carrito de prueba, vacio al empezar cada prueba. */
    private Cart cart;

    /** Se ejecuta antes de cada prueba. */
    @BeforeEach
    void setUp() {
        catalog = new FallbackCatalogService();
        verifier = new CartVerifier(catalog);
        cart = new Cart(SampleUsers.CUSTOMER);
    }

    /** Criterio 1: un carrito bueno no tiene problemas y sus subtotales usan el precio vigente del catalogo. */
    @Test
    void validCartUsesCurrentCatalogPrice() {
        cart.addProduct(PRODUCT, COMMERCE, 3);
        BigDecimal price = catalog.findProduct(PRODUCT).orElseThrow().price();
        BigDecimal expected = price.multiply(BigDecimal.valueOf(3));

        CartVerification result = verifier.verify(cart);

        assertTrue(result.isValid());
        assertEquals(1, result.lines().size());
        assertEquals(COMMERCE, result.lines().get(0).commerceId());
        assertEquals(price, result.lines().get(0).unitPrice());
        assertEquals(expected, result.lines().get(0).lineSubtotal());
        assertEquals(expected, result.subtotal());
    }

    /** Criterio 2: si la cantidad supera el stock (11 > 10), informa cual producto cambio. */
    @Test
    void reportsProductWithInsufficientStock() {
        cart.addProduct(PRODUCT, COMMERCE, 11);
        String name = catalog.findProduct(PRODUCT).orElseThrow().name();

        CartVerification result = verifier.verify(cart);

        assertFalse(result.isValid());
        assertEquals(1, result.issues().size());
        CartVerification.Issue issue = result.issues().get(0);
        assertEquals(PRODUCT, issue.productId());
        assertEquals(name, issue.productName());
        assertEquals(CartVerification.IssueType.NOT_AVAILABLE, issue.type());
        assertEquals(11, issue.requestedQuantity());
    }

    /** Un producto agotado se informa como no disponible. */
    @Test
    void reportsSoldOutProduct() {
        cart.addProduct(SOLD_OUT, COMMERCE, 1);

        CartVerification result = verifier.verify(cart);

        assertEquals(1, result.issues().size());
        assertEquals(CartVerification.IssueType.NOT_AVAILABLE, result.issues().get(0).type());
    }

    /** Un producto que el catalogo ya no conoce se informa como "ya no existe" y no genera linea. */
    @Test
    void reportsProductThatNoLongerExists() {
        UUID ghost = UUID.randomUUID();
        cart.addProduct(ghost, COMMERCE, 2);

        CartVerification result = verifier.verify(cart);

        assertEquals(1, result.issues().size());
        assertEquals(ghost, result.issues().get(0).productId());
        assertEquals(CartVerification.IssueType.NO_LONGER_EXISTS, result.issues().get(0).type());
        assertNull(result.issues().get(0).productName());
        assertTrue(result.lines().isEmpty());
    }

    /** Informa todos los productos con problemas, no solo el primero. */
    @Test
    void reportsAllProblemsAtOnce() {
        cart.addProduct(PRODUCT, COMMERCE, 11);
        cart.addProduct(SOLD_OUT, COMMERCE, 1);
        cart.addProduct(UUID.randomUUID(), COMMERCE, 1);

        CartVerification result = verifier.verify(cart);

        assertEquals(3, result.issues().size());
    }

    /** verifyOrThrow detiene la operacion con un error que nombra el producto, y el carrito queda igual. */
    @Test
    void verifyOrThrowStopsAndKeepsTheCartUntouched() {
        cart.addProduct(PRODUCT, COMMERCE, 11);
        String name = catalog.findProduct(PRODUCT).orElseThrow().name();

        ProductUnavailableException error = assertThrows(ProductUnavailableException.class,
                () -> verifier.verifyOrThrow(cart));

        assertTrue(error.getMessage().contains(name));
        assertEquals(List.of(PRODUCT), error.getProductIds());
        assertEquals(11, cart.quantityOf(PRODUCT));
        assertEquals(1, cart.getItems().size());
    }

    /** Si todo esta tranquilo, verifyOrThrow no lanza nada y devuelve la verificacion. */
    @Test
    void verifyOrThrowReturnsTheVerificationWhenEverythingIsFine() {
        cart.addProduct(PRODUCT, COMMERCE, 2);

        CartVerification result = verifier.verifyOrThrow(cart);

        assertTrue(result.isValid());
    }

    /** Un carrito vacio no tiene problemas y su subtotal es cero. */
    @Test
    void emptyCartIsValid() {
        CartVerification result = verifier.verify(cart);

        assertTrue(result.isValid());
        assertEquals(BigDecimal.ZERO, result.subtotal());
    }
}
