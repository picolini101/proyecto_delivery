package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.port.in.StartCheckoutCommand;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.EmptyCartException;
import org.jala.edu.order.domain.exception.ProductUnavailableException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StartCheckoutUseCaseTest {

    private UserService userService;
    private CartRepositoryPort cartRepository;
    private CartVerifier cartVerifier;
    private OrderRepositoryPort orderRepository;
    private StartCheckoutUseCase useCase;

    private final UUID customerId = SampleUsers.CUSTOMER;
    private final UUID inactiveCustomerId = SampleUsers.INACTIVE_CUSTOMER;
    private final UUID courierId = SampleUsers.COURIER;
    private final UUID productId = SampleCatalog.PIZZA;
    private final UUID soldOutProductId = SampleCatalog.EMPANADA;
    private final UUID commerceId = SampleCatalog.COMMERCE;

    @BeforeEach
    void setUp() {
        userService = new FallbackUserService();
        cartRepository = new InMemoryCartRepositoryAdapter();
        cartVerifier = new CartVerifier(new FallbackCatalogService());
        orderRepository = new InMemoryOrderRepositoryAdapter();

        useCase = new StartCheckoutUseCase(userService, cartRepository, cartVerifier, orderRepository);
    }

    @Test
    @DisplayName("Criterio 3: Falla si el usuario es un cliente inactivo")
    void shouldFailWhenCustomerIsInactive() {
        StartCheckoutCommand command = new StartCheckoutCommand(inactiveCustomerId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedCustomerException.class);
    }

    @Test
    @DisplayName("Criterio 3: Falla si el usuario no tiene rol CLIENTE (ej. Repartidor)")
    void shouldFailWhenUserIsNotCustomer() {
        StartCheckoutCommand command = new StartCheckoutCommand(courierId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedCustomerException.class);
    }

    @Test
    @DisplayName("Criterio 2: Falla si el cliente no tiene carrito guardado")
    void shouldFailWhenCartDoesNotExist() {
        StartCheckoutCommand command = new StartCheckoutCommand(customerId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("Criterio 2: Falla si el carrito existe pero está vacío")
    void shouldFailWhenCartIsEmpty() {
        Cart cart = new Cart(customerId);
        cartRepository.save(cart);

        StartCheckoutCommand command = new StartCheckoutCommand(customerId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    @DisplayName("Precondición Catálogo: Falla si un producto está agotado")
    void shouldFailWhenProductIsSoldOut() {
        Cart cart = new Cart(customerId);
        cart.addProduct(soldOutProductId, commerceId, 1);
        cartRepository.save(cart);

        StartCheckoutCommand command = new StartCheckoutCommand(customerId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    @DisplayName("Criterio 1: Inicia exitosamente la compra, crea Pedido en CREATED y vacía el carrito")
    void shouldCreateOrderAndClearCartSuccessfully() {
        Cart cart = new Cart(customerId);
        cart.addProduct(productId, commerceId, 2);
        cartRepository.save(cart);

        StartCheckoutCommand command = new StartCheckoutCommand(customerId);
        OrderSummary summary = useCase.execute(command);

        assertThat(summary).isNotNull();
        assertThat(summary.customerId()).isEqualTo(customerId);
        assertThat(summary.status()).isEqualTo(OrderStatus.CREATED.name());
        assertThat(summary.totalItems()).isEqualTo(2);
        assertThat(summary.subtotal()).isEqualByComparingTo("100.00");

        assertThat(orderRepository.findByCustomerId(new org.jala.edu.order.domain.model.CustomerId(customerId)))
                .hasSize(1);

        Cart cartAfter = cartRepository.findByCustomerId(customerId).orElseThrow();
        assertThat(cartAfter.getItems()).isEmpty();
    }
}