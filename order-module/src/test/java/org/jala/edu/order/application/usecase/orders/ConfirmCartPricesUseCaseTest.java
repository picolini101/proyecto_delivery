package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.infrastructure.adapter.out.catalog.CatalogGatewayAdapter;
import org.jala.edu.order.infrastructure.adapter.out.catalog.UnavailableCatalogPort;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link ConfirmCartPricesUseCase}: el cliente acepta los precios nuevos de su carrito.
 * El carrito de cada prueba tiene la pizza con precio guardado 40.00, y el catalogo de prueba la vende a 50.00.
 */
class ConfirmCartPricesUseCaseTest {

    /** Precio que el cliente vio antes de que cambiara el catalogo. */
    private static final BigDecimal SEEN_PRICE = new BigDecimal("40.00");

    private InMemoryCartRepositoryAdapter carts;
    private CartVerifier verifier;
    private ConfirmCartPricesUseCase useCase;

    @BeforeEach
    void setUp() {
        carts = new InMemoryCartRepositoryAdapter();
        verifier = new CartVerifier(new CatalogGatewayAdapter(new FallbackCatalogService()));
        useCase = new ConfirmCartPricesUseCase(new FallbackUserService(), carts, verifier);

        Cart cart = carts.findOrCreate(SampleUsers.CUSTOMER);
        cart.addProduct(SampleCatalog.PIZZA, SampleCatalog.COMMERCE, 2, SEEN_PRICE);
        carts.save(cart);
    }

    /** Si el cliente acepta el precio que vio, el carrito guarda el precio nuevo y ya no hay cambios pendientes. */
    @Test
    void savesTheNewPriceTheCustomerAccepted() {
        List<CartVerification.PriceChange> shown =
                verifier.verify(carts.findOrCreate(SampleUsers.CUSTOMER)).priceChanges();

        Cart cart = useCase.execute(SampleUsers.CUSTOMER, shown);

        assertEquals(0, SampleCatalog.PIZZA_PRICE.compareTo(cart.findItem(SampleCatalog.PIZZA).getUnitPrice()));
        assertEquals(2, cart.quantityOf(SampleCatalog.PIZZA));
        assertFalse(verifier.verify(cart).hasPriceChanges());
    }

    /** Si el catalogo cambio el precio otra vez despues del aviso, ese precio no se acepta a ciegas. */
    @Test
    void doesNotAcceptAPriceTheCustomerNeverSaw() {
        CartVerification.PriceChange notShown = new CartVerification.PriceChange(
                SampleCatalog.PIZZA, "Pizza", SEEN_PRICE, new BigDecimal("99.00"));

        Cart cart = useCase.execute(SampleUsers.CUSTOMER, List.of(notShown));

        assertEquals(0, SEEN_PRICE.compareTo(cart.findItem(SampleCatalog.PIZZA).getUnitPrice()));
    }

    /** Solo un cliente activo puede aceptar precios. */
    @Test
    void rejectsAUserWhoIsNotACustomer() {
        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(SampleUsers.COURIER, List.of()));
    }

    /** Catalogo caido: el error llega a la pantalla y el carrito queda con el precio anterior. */
    @Test
    void keepsTheCartWhenTheCatalogIsUnavailable() {
        ConfirmCartPricesUseCase downCatalog = new ConfirmCartPricesUseCase(
                new FallbackUserService(), carts, new CartVerifier(new UnavailableCatalogPort()));

        assertThrows(CatalogUnavailableException.class, () -> downCatalog.execute(SampleUsers.CUSTOMER, List.of()));

        Cart cart = carts.findOrCreate(SampleUsers.CUSTOMER);
        assertEquals(0, SEEN_PRICE.compareTo(cart.findItem(SampleCatalog.PIZZA).getUnitPrice()));
    }
}