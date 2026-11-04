package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link IncreaseCartItemQuantityUseCase} (HU4).
 * Usa el catalogo y los usuarios de prueba del core (Fallback...) y el repositorio en memoria.
 * Con el catalogo de prueba, el producto AVAILABLE tiene stock 10.
 * Para dejar un producto en el carrito se usa el caso de uso de HU1 ({@link AddProductToCartUseCase}).
 * {@code assertThrows(Clase.class, () -> codigo)} comprueba que el codigo lance ese error;
 * si no lo lanza, la prueba falla.
 */
class IncreaseCartItemQuantityUseCaseTest {

    /** Producto de prueba con stock 10. */
    private static final UUID PRODUCT = FallbackCatalogService.AVAILABLE_PRODUCT;

    /** Repositorio que se comparte entre los dos casos de uso. */
    private InMemoryCartRepositoryAdapter carts;

    /** Caso de uso de HU1, para dejar productos en el carrito antes de cada prueba. */
    private AddProductToCartUseCase addToCart;

    /** Caso de uso que se esta probando. */
    private IncreaseCartItemQuantityUseCase useCase;

    /** Se ejecuta antes de cada prueba, crea un repositorio vacio y arma los dos casos de uso. */
    @BeforeEach
    void setUp() {
        carts = new InMemoryCartRepositoryAdapter();
        FallbackCatalogService catalog = new FallbackCatalogService();
        FallbackUserService users = new FallbackUserService();
        addToCart = new AddProductToCartUseCase(carts, catalog, users);
        useCase = new IncreaseCartItemQuantityUseCase(carts, catalog, users);
    }

    /** Criterio 1: la cantidad se suma a la linea existente, no se crea otra fila. */
    @Test
    void sumsQuantityIntoExistingLine() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 3);

        useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 4);

        assertEquals(7, quantityInCart());
        assertEquals(1, carts.findOrCreate(SampleUsers.CUSTOMER).getItems().size());
    }

    /** Caso borde: si la cantidad acumulada es igual al stock se acepta. */
    @Test
    void acceptsWhenAccumulatedQuantityEqualsStock() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 6);

        useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 4);

        assertEquals(10, quantityInCart());
    }

    /** Criterio 2: si lo acumulado supera el stock (7 + 4 = 11 > 10) se rechaza y queda la cantidad anterior (7). */
    @Test
    void rejectsAndKeepsPreviousQuantityWhenStockIsNotEnough() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 7);

        ConflictException error = assertThrows(ConflictException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 4));

        assertEquals("No se tienen suficientes unidades en stock", error.getMessage());
        assertEquals(7, quantityInCart());
    }

    /** Una cantidad enorme no debe dar la vuelta y colarse como negativa: se rechaza y no cambia nada. */
    @Test
    void rejectsQuantityThatOverflows() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 3);

        assertThrows(ConflictException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, Integer.MAX_VALUE));

        assertEquals(3, quantityInCart());
    }

    /** Cantidad cero o negativa: lanza DomainException y el carrito no cambia. */
    @Test
    void rejectsZeroOrNegativeQuantity() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 3);

        DomainException zero = assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 0));
        DomainException negative = assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, -1));

        assertEquals("Quantity must be greater than zero", zero.getMessage());
        assertEquals("Quantity must be greater than zero", negative.getMessage());
        assertEquals(3, quantityInCart());
    }

    /** Si el producto no esta en el carrito, lanza NotFoundException. */
    @Test
    void rejectsProductNotInCart() {
        assertThrows(NotFoundException.class, () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 1));
    }

    /** Un cliente inactivo no esta autorizado: lanza DomainException. */
    @Test
    void rejectsInactiveCustomer() {
        DomainException error = assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.INACTIVE_CUSTOMER, PRODUCT, 1));

        assertEquals("User not authorized for this operation", error.getMessage());
    }

    /** Lee cuantas unidades de PRODUCT tiene el carrito del cliente de prueba. */
    private int quantityInCart() {
        return carts.findOrCreate(SampleUsers.CUSTOMER).quantityOf(PRODUCT);
    }
}
