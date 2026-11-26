package org.jala.edu.order.infrastructure.adapter.out.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link CatalogGatewayAdapter} (INT-001).
 * Los casos felices usan el catalogo de prueba del core: la pizza tiene stock 10 y la empanada esta agotada.
 * Los casos de precio y de falla usan un catalogo falso que la prueba controla.
 */
class CatalogGatewayAdapterTest {

    /** Catalogo de prueba del core. */
    private FallbackCatalogService fallback;

    /** Clase que se esta probando, conectada al catalogo de prueba. */
    private CatalogGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        fallback = new FallbackCatalogService();
        adapter = new CatalogGatewayAdapter(fallback);
    }

    /** Producto disponible: devuelve su comercio, nombre y precio, y confirma que hay stock. */
    @Test
    void returnsAvailableProductWithItsData() {
        ProductInfo product = adapter.findProduct(SampleCatalog.PIZZA).orElseThrow();

        assertEquals(SampleCatalog.PIZZA, product.productId());
        assertEquals(SampleCatalog.COMMERCE, product.commerceId());
        assertEquals(SampleCatalog.PIZZA_NAME, product.name());
        assertEquals(0, SampleCatalog.PIZZA_PRICE.compareTo(product.price()));
        assertTrue(adapter.isAvailable(SampleCatalog.PIZZA, 1));
    }

    /** Producto sin stock: el catalogo lo conoce pero el adaptador lo informa como no disponible. */
    @Test
    void reportsSoldOutProductAsNotAvailable() {
        assertTrue(adapter.findProduct(SampleCatalog.EMPANADA).isPresent());
        assertFalse(adapter.isAvailable(SampleCatalog.EMPANADA, 1));
    }

    /** Pedir mas unidades que el stock (11 > 10) tampoco esta disponible. */
    @Test
    void reportsQuantityAboveStockAsNotAvailable() {
        assertFalse(adapter.isAvailable(SampleCatalog.PIZZA, SampleCatalog.PIZZA_STOCK + 1));
    }

    /** Un producto que el catalogo no conoce no es una falla: es un resultado vacio. */
    @Test
    void returnsEmptyForUnknownProduct() {
        assertTrue(adapter.findProduct(UUID.randomUUID()).isEmpty());
    }

    /** Cambio de precio: el adaptador no guarda nada, asi que siempre devuelve el precio vigente. */
    @Test
    void returnsTheCurrentPriceAfterTheCatalogChangesIt() {
        UUID productId = UUID.randomUUID();
        StubCatalogService stub = new StubCatalogService();
        stub.put(new ProductInfo(productId, SampleCatalog.COMMERCE, "Pizza", new BigDecimal("50.00")));
        adapter = new CatalogGatewayAdapter(stub);
        assertEquals(0, new BigDecimal("50.00").compareTo(adapter.findProduct(productId).orElseThrow().price()));

        stub.put(new ProductInfo(productId, SampleCatalog.COMMERCE, "Pizza", new BigDecimal("65.00")));

        assertEquals(0, new BigDecimal("65.00").compareTo(adapter.findProduct(productId).orElseThrow().price()));
    }

    /** Lista solo lo que el catalogo ofrece como disponible (la empanada agotada no aparece). */
    @Test
    void listsTheAvailableProducts() {
        List<ProductInfo> products = adapter.findAvailableProducts();

        assertEquals(1, products.size());
        assertEquals(SampleCatalog.PIZZA, products.get(0).productId());
    }

    /** Servicio caido al buscar un producto: se convierte en CatalogUnavailableException. */
    @Test
    void translatesAFailureWhenFindingAProduct() {
        StubCatalogService stub = new StubCatalogService();
        stub.failWith(new IllegalStateException("db down"));
        adapter = new CatalogGatewayAdapter(stub);

        assertThrows(CatalogUnavailableException.class, () -> adapter.findProduct(SampleCatalog.PIZZA));
    }

    /** Servicio caido al consultar disponibilidad. */
    @Test
    void translatesAFailureWhenCheckingAvailability() {
        StubCatalogService stub = new StubCatalogService();
        stub.failWith(new IllegalStateException("db down"));
        adapter = new CatalogGatewayAdapter(stub);

        assertThrows(CatalogUnavailableException.class, () -> adapter.isAvailable(SampleCatalog.PIZZA, 1));
    }

    /** Servicio caido al listar productos. */
    @Test
    void translatesAFailureWhenListingProducts() {
        StubCatalogService stub = new StubCatalogService();
        stub.failWith(new IllegalStateException("db down"));
        adapter = new CatalogGatewayAdapter(stub);

        assertThrows(CatalogUnavailableException.class, () -> adapter.findAvailableProducts());
    }

    /** El mensaje es amigable y la falla original queda como causa para depurar. */
    @Test
    void keepsAFriendlyMessageAndTheOriginalCause() {
        IllegalStateException failure = new IllegalStateException("db down");
        StubCatalogService stub = new StubCatalogService();
        stub.failWith(failure);
        adapter = new CatalogGatewayAdapter(stub);

        CatalogUnavailableException error = assertThrows(CatalogUnavailableException.class,
                () -> adapter.findProduct(SampleCatalog.PIZZA));

        assertEquals(CatalogUnavailableException.MESSAGE, error.getMessage());
        assertSame(failure, error.getCause());
    }

    /** Una respuesta null del servicio tampoco es utilizable: se trata como servicio no disponible. */
    @Test
    void treatsANullAnswerAsUnavailable() {
        StubCatalogService stub = new StubCatalogService();
        stub.answerNull();
        adapter = new CatalogGatewayAdapter(stub);

        assertThrows(CatalogUnavailableException.class, () -> adapter.findProduct(SampleCatalog.PIZZA));
        assertThrows(CatalogUnavailableException.class, () -> adapter.findAvailableProducts());
    }

    /** Catalogo falso: la prueba decide que productos tiene, si falla y si responde null. */
    private static class StubCatalogService implements CatalogService {

        private final Map<UUID, ProductInfo> products = new HashMap<>();
        private RuntimeException failure;
        private boolean answerNull;

        void put(ProductInfo product) {
            products.put(product.productId(), product);
        }

        void failWith(RuntimeException error) {
            this.failure = error;
        }

        void answerNull() {
            this.answerNull = true;
        }

        private void failIfAsked() {
            if (failure != null) {
                throw failure;
            }
        }

        @Override
        public Optional<ProductInfo> findProduct(UUID productId) {
            failIfAsked();
            return answerNull ? null : Optional.ofNullable(products.get(productId));
        }

        @Override
        public List<ProductInfo> findAvailableProducts() {
            failIfAsked();
            return answerNull ? null : List.copyOf(products.values());
        }

        @Override
        public boolean isAvailable(UUID productId, int quantity) {
            failIfAsked();
            return products.containsKey(productId) && quantity > 0;
        }

        @Override
        public void reserveStock(UUID productId, int quantity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void releaseStock(UUID productId, int quantity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UUID> findCommerceIdsByMerchant(UUID merchantId) {
            return List.of();
        }
    }
}