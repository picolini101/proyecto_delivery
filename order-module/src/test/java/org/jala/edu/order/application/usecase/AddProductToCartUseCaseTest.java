package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddProductToCartUseCaseTest {

    private CartRepositoryPort carts;
    private AddProductToCartUseCase useCase;

    @BeforeEach
    void setUp() {
        carts = mock(CartRepositoryPort.class);
        when(carts.findByCustomerId(any())).thenReturn(Optional.empty());
        when(carts.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        useCase = new AddProductToCartUseCase(carts, new FallbackCatalogService(), new FallbackUserService());
    }

    @Test
    void addsAvailableProduct() {
        Cart cart = useCase.execute(SampleUsers.CUSTOMER, FallbackCatalogService.AVAILABLE_PRODUCT, 2);
        assertEquals(2, cart.quantityOf(FallbackCatalogService.AVAILABLE_PRODUCT));
    }

    @Test
    void rejectsQuantityAboveStock() {
        assertThrows(ConflictException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, FallbackCatalogService.AVAILABLE_PRODUCT, 11));
    }

    @Test
    void rejectsSoldOutProduct() {
        assertThrows(ConflictException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, FallbackCatalogService.SOLD_OUT_PRODUCT, 1));
    }

    @Test
    void rejectsInactiveCustomer() {
        assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.INACTIVE_CUSTOMER, FallbackCatalogService.AVAILABLE_PRODUCT, 1));
    }

    @Test
    void rejectsUnknownProduct() {
        assertThrows(DomainException.class,
                () -> useCase.execute(SampleUsers.CUSTOMER, UUID.randomUUID(), 1));
    }
}
