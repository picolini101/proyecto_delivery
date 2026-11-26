package org.jala.edu.order.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.order.domain.exception.PriceChangedException;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.order.domain.exception.ProductUnavailableException;
import org.jala.edu.order.domain.model.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.jala.edu.order.infrastructure.adapter.out.catalog.CatalogGatewayAdapter;
import org.jala.edu.order.infrastructure.adapter.out.catalog.UnavailableCatalogPort;

/**
 * Pruebas de {@link CartVerifier} (HU6).
 * Usa el catalogo de prueba del core: el producto AVAILABLE tiene stock 10 y el producto SOLD_OUT no tiene stock.
 * Un producto con un id cualquiera (UUID.randomUUID()) no existe en el catalogo.
 */
class CartVerifierTest {

    /** Producto de prueba con stock 10. */
    private static final UUID PRODUCT = SampleCatalog.PIZZA;

    /** Producto de prueba agotado. */
    private static final UUID SOLD_OUT = SampleCatalog.EMPANADA;

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
        verifier = new CartVerifier(new CatalogGatewayAdapter(catalog));
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

    /** Catalogo caido: verify no inventa resultados, lanza el error y el carrito queda igual. */
    @Test
    void verifyFailsWhenTheCatalogIsUnavailableAndKeepsTheCartUntouched() {
        verifier = new CartVerifier(new UnavailableCatalogPort());
        cart.addProduct(PRODUCT, COMMERCE, 2);

        assertThrows(CatalogUnavailableException.class, () -> verifier.verify(cart));

        assertEquals(2, cart.quantityOf(PRODUCT));
        assertEquals(1, cart.getItems().size());
    }

    /** Catalogo caido: verifyOrThrow tambien informa el error del catalogo, no "producto no disponible". */
    @Test
    void verifyOrThrowReportsTheUnavailableCatalog() {
        verifier = new CartVerifier(new UnavailableCatalogPort());
        cart.addProduct(PRODUCT, COMMERCE, 2);

        assertThrows(CatalogUnavailableException.class, () -> verifier.verifyOrThrow(cart));
    }

    /** Cambio de precio: el cliente vio 40.00 y el catalogo ya vale mas; se informa y se usa el precio vigente. */
    @Test
    void reportsAPriceChange() {
        cart.addProduct(PRODUCT, COMMERCE, 2, new BigDecimal("40.00"));
        BigDecimal current = catalog.findProduct(PRODUCT).orElseThrow().price();

        CartVerification result = verifier.verify(cart);

        assertTrue(result.isValid());
        assertTrue(result.hasPriceChanges());
        assertEquals(1, result.priceChanges().size());
        CartVerification.PriceChange change = result.priceChanges().get(0);
        assertEquals(PRODUCT, change.productId());
        assertEquals(0, new BigDecimal("40.00").compareTo(change.oldPrice()));
        assertEquals(0, current.compareTo(change.newPrice()));
        assertEquals(0, current.compareTo(result.lines().get(0).unitPrice()));
    }

    /** Si el precio que vio el cliente es el mismo del catalogo, no hay cambio que confirmar. */
    @Test
    void reportsNoPriceChangeWhenThePriceIsTheSame() {
        BigDecimal current = catalog.findProduct(PRODUCT).orElseThrow().price();
        cart.addProduct(PRODUCT, COMMERCE, 2, current);

        assertFalse(verifier.verify(cart).hasPriceChanges());
    }

    /** Una linea sin precio guardado no genera cambios de precio. */
    @Test
    void ignoresLinesWithoutASavedPrice() {
        cart.addProduct(PRODUCT, COMMERCE, 2);

        assertFalse(verifier.verify(cart).hasPriceChanges());
    }

    /** verifyOrThrow se detiene si hay un precio sin confirmar, nombra el producto y deja el carrito igual. */
    @Test
    void verifyOrThrowStopsWhenAPriceChangedAndKeepsTheCartUntouched() {
        cart.addProduct(PRODUCT, COMMERCE, 2, new BigDecimal("40.00"));
        String name = catalog.findProduct(PRODUCT).orElseThrow().name();

        PriceChangedException error = assertThrows(PriceChangedException.class, () -> verifier.verifyOrThrow(cart));

        assertTrue(error.getMessage().contains(name));
        assertEquals(List.of(PRODUCT), error.getProductIds());
        assertEquals(0, new BigDecimal("40.00").compareTo(cart.findItem(PRODUCT).getUnitPrice()));
    }
}
