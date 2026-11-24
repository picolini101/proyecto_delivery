package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.order.domain.exception.ProductNotFoundInCartException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RemoveProductFromCartUseCaseTest {

    private InMemoryCartRepositoryAdapter cartRepository;
    private FallbackCatalogService catalogService;
    private RemoveProductFromCartUseCase removeUseCase;
    private GetCartSummaryUseCase summaryUseCase;

    private static final UUID CUSTOMER = SampleUsers.CUSTOMER;
    private static final UUID PRODUCT_AVAILABLE = SampleCatalog.PIZZA;
    private static final UUID PRODUCT_SOLD_OUT = SampleCatalog.EMPANADA;

    @BeforeEach
    void setUp() {
        cartRepository = new InMemoryCartRepositoryAdapter();
        catalogService = new FallbackCatalogService();
        removeUseCase = new RemoveProductFromCartUseCase(cartRepository);
        summaryUseCase = new GetCartSummaryUseCase(cartRepository, catalogService);
    }

    @Test
    @DisplayName("Criterio 1: Eliminar un producto de varios recalcula el subtotal y total restante")
    void shouldRemoveProductAndRecalculateTotals() {
        // GIVEN: Un carrito con productos agregados usando el catalogo estandar (SampleCatalog)
        Cart cart = cartRepository.findOrCreate(CUSTOMER);
        cart.addProduct(PRODUCT_AVAILABLE, SampleCatalog.COMMERCE, 2);
        cartRepository.save(cart);

        CartSummary initialSummary = summaryUseCase.execute(CUSTOMER);
        assertTrue(initialSummary.subtotal().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(initialSummary.total().compareTo(BigDecimal.ZERO) > 0);

        // WHEN: El cliente elimina la linea de PIZZA
        Cart updatedCart = removeUseCase.execute(CUSTOMER, PRODUCT_AVAILABLE);

        // THEN: El producto ya no esta en el carrito y el resumen se actualiza a 0.00
        assertEquals(0, updatedCart.quantityOf(PRODUCT_AVAILABLE));
        CartSummary finalSummary = summaryUseCase.execute(CUSTOMER);
        assertEquals(BigDecimal.ZERO, finalSummary.subtotal());
        assertEquals(BigDecimal.ZERO, finalSummary.shipping());
        assertEquals(BigDecimal.ZERO, finalSummary.total());
    }

    @Test
    @DisplayName("Criterio 2: Eliminar el unico producto deja el carrito vacio y totales en cero exacto")
    void shouldResetTotalsToZeroWhenLastItemRemoved() {
        // Carrito con un unico producto registrado
        Cart cart = cartRepository.findOrCreate(CUSTOMER);
        cart.addProduct(PRODUCT_AVAILABLE, SampleCatalog.COMMERCE, 1);
        cartRepository.save(cart);

        // WHEN: Se remueve el unico producto existente
        removeUseCase.execute(CUSTOMER, PRODUCT_AVAILABLE);

        // THEN: Carrito vacio y todos los importes en exactamente 0.00
        CartSummary summary = summaryUseCase.execute(CUSTOMER);
        assertTrue(summary.groups().isEmpty(), "No deben quedar grupos de comercios");
        assertEquals(BigDecimal.ZERO, summary.subtotal());
        assertEquals(BigDecimal.ZERO, summary.shipping());
        assertEquals(BigDecimal.ZERO, summary.total());
    }

    @Test
    @DisplayName("Criterio 3: Intentar remover producto no existente lanza ProductNotFoundInCartException")
    void shouldThrowProductNotFoundWhenItemNotInCart() {
        // GIVEN: Carrito que contiene unicamente un producto
        Cart cart = cartRepository.findOrCreate(CUSTOMER);
        cart.addProduct(PRODUCT_AVAILABLE, SampleCatalog.COMMERCE, 1);
        cartRepository.save(cart);

        UUID unaddedProductId = UUID.randomUUID();

        // WHEN & THEN: Se lanza la excepcion de dominio sin mutar las lineas existentes
        assertThrows(ProductNotFoundInCartException.class, () ->
                removeUseCase.execute(CUSTOMER, unaddedProductId)
        );

        Cart reloadedCart = cartRepository.findOrCreate(CUSTOMER);
        assertEquals(1, reloadedCart.getItems().size());
    }
}