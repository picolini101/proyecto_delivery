package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link DecreaseCartItemQuantityUseCase} (boton "-" del carrito).
 * Para dejar un producto en el carrito se usa el caso de uso de HU1 ({@link AddProductToCartUseCase}).
 */
class DecreaseCartItemQuantityUseCaseTest {

    /** Producto de prueba con stock 10. */
    private static final UUID PRODUCT = FallbackCatalogService.AVAILABLE_PRODUCT;

    /** Repositorio que se comparte entre los dos casos de uso. */
    private InMemoryCartRepositoryAdapter carts;

    /** Caso de uso de HU1, para dejar productos en el carrito antes de cada prueba. */
    private AddProductToCartUseCase addToCart;

    /** Caso de uso que se esta probando. */
    private DecreaseCartItemQuantityUseCase useCase;

    /** Se ejecuta antes de cada prueba: crea un repositorio vacio y arma los dos casos de uso. */
    @BeforeEach
    void setUp() {
        carts = new InMemoryCartRepositoryAdapter();
        FallbackUserService users = new FallbackUserService();
        addToCart = new AddProductToCartUseCase(carts, new FallbackCatalogService(), users);
        useCase = new DecreaseCartItemQuantityUseCase(carts, users);
    }

    /** Quita 1 unidad a una linea de 5, queda en 4, en la misma fila. */
    @Test
    void decreasesQuantityOfExistingLine() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 5);

        useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 1);

        assertEquals(4, quantityInCart());
        assertEquals(1, carts.findOrCreate(SampleUsers.CUSTOMER).getItems().size());
    }

    /** La cantidad no puede bajar de 1, con 1 unidad, quitar 1 se rechaza y queda 1. */
    @Test
    void rejectsDecreaseBelowOne() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 1);

        DomainException error = assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 1));

        assertEquals("La cantidad no puede quedar en menos de 1", error.getMessage());
        assertEquals(1, quantityInCart());
    }

    /** Quitar mas unidades de las que hay (2 - 5) tambien se rechaza y no cambia nada. */
    @Test
    void rejectsRemovingMoreThanTheCartHas() {
        addToCart.execute(SampleUsers.CUSTOMER, PRODUCT, 2);

        assertThrows(DomainException.class, () -> useCase.execute(SampleUsers.CUSTOMER, PRODUCT, 5));

        assertEquals(2, quantityInCart());
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
