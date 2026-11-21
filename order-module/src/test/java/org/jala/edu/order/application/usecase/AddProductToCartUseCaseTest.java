package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.jala.edu.order.infrastructure.adapter.out.catalog.CatalogGatewayAdapter;
import org.jala.edu.order.infrastructure.adapter.out.catalog.UnavailableCatalogPort;

/**
 * Pruebas de {@link AddProductToCartUseCase}.
 * Usa el catalogo y los usuarios de prueba del core (Fallback...) y el repositorio en memoria.
 * Con el catalogo de prueba: el producto AVAILABLE tiene stock 10 y el producto SOLD_OUT no tiene stock.
 * <p>
 * En las pruebas de error, el codigo dentro de {@code try} debe lanzar el error esperado.
 * Si no lo lanza, {@code fail(...)} hace fallar la prueba; si lo lanza, entra al {@code catch} y la prueba pasa.
 */
class AddProductToCartUseCaseTest {

    /** Caso de uso que se esta probando. */
    private AddProductToCartUseCase useCase;

    /** Se ejecuta antes de cada prueba: crea un repositorio vacio y arma el caso de uso. */
    @BeforeEach
    void setUp() {
        InMemoryCartRepositoryAdapter carts = new InMemoryCartRepositoryAdapter();
        useCase = new AddProductToCartUseCase(carts, new CatalogGatewayAdapter(new FallbackCatalogService()),
                new FallbackUserService());
    }

    /** Un cliente activo agrega 2 unidades de un producto con stock: el carrito queda con 2. */
    @Test
    void addsAvailableProduct() {
        Cart cart = useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.PIZZA, 2);
        assertEquals(2, cart.quantityOf(SampleCatalog.PIZZA));
    }

    /** Agregar dos veces el mismo producto suma las cantidades (3 + 4 = 7, que todavia cabe en el stock de 10). */
    @Test
    void sumsQuantityOnSecondAdd() {
        useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.PIZZA, 3);
        Cart cart = useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.PIZZA, 4);
        assertEquals(7, cart.quantityOf(SampleCatalog.PIZZA));
    }

    /** Pedir 11 unidades cuando el stock es 10 lanza ConflictException. */
    @Test
    void rejectsQuantityAboveStock() {
        try {
            useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.PIZZA, 11);
            fail("Debia lanzar ConflictException");
        } catch (ConflictException e) {
            // Era lo esperado
        }
    }

    /** Un producto agotado no se puede agregar: lanza ConflictException. */
    @Test
    void rejectsSoldOutProduct() {
        try {
            useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.EMPANADA, 1);
            fail("Debia lanzar ConflictException");
        } catch (ConflictException e) {
            // Era lo esperado
        }
    }

    /** Un cliente inactivo no esta autorizado: lanza DomainException. */
    @Test
    void rejectsInactiveCustomer() {
        try {
            useCase.execute(SampleUsers.INACTIVE_CUSTOMER, SampleCatalog.PIZZA, 1);
            fail("Debia lanzar DomainException");
        } catch (DomainException e) {
            // Era lo esperado
        }
    }

    /** Un producto que no existe en el catalogo lanza error (NotFoundException es un tipo de DomainException). */
    @Test
    void rejectsUnknownProduct() {
        try {
            useCase.execute(SampleUsers.CUSTOMER, UUID.randomUUID(), 1);
            fail("Debia lanzar DomainException");
        } catch (DomainException e) {
            // Era lo esperado
        }
    }
    /** Catalogo caido: no se agrega nada, el carrito queda vacio y se informa el error del catalogo. */
    @Test
    void doesNotChangeTheCartWhenTheCatalogIsUnavailable() {
        InMemoryCartRepositoryAdapter carts = new InMemoryCartRepositoryAdapter();
        AddProductToCartUseCase downCatalog =
                new AddProductToCartUseCase(carts, new UnavailableCatalogPort(), new FallbackUserService());

        assertThrows(CatalogUnavailableException.class,
                () -> downCatalog.execute(SampleUsers.CUSTOMER, FallbackCatalogService.AVAILABLE_PRODUCT, 2));

        assertTrue(carts.findOrCreate(SampleUsers.CUSTOMER).getItems().isEmpty());
    }

    /** Al agregar, el carrito guarda el precio que el cliente ve en el catalogo en ese momento. */
    @Test
    void savesThePriceTheCustomerSees() {
        Cart cart = useCase.execute(SampleUsers.CUSTOMER, SampleCatalog.PIZZA, 2);
        assertEquals(0, SampleCatalog.PIZZA_PRICE.compareTo(cart.findItem(SampleCatalog.PIZZA).getUnitPrice()));
    }
}