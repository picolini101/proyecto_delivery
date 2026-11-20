package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.dto.CheckoutSummary;
import org.jala.edu.order.application.port.in.StartCheckoutCommand;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreviewCheckoutSummaryUseCaseTest {

    @Mock
    private UserService userService;

    @Mock
    private CartRepositoryPort cartRepository;

    @Mock
    private CartVerifier cartVerifier;

    private PreviewCheckoutSummaryUseCase useCase;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new PreviewCheckoutSummaryUseCase(userService, cartRepository, cartVerifier);
    }

    @Test
    void shouldRejectsInactiveCustomer() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(false);
        StartCheckoutCommand command = new StartCheckoutCommand(CUSTOMER_ID);
        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(command));

        // En esta situacion verifica que ni siquiera haya consultado el carrito
        verifyNoInteractions(cartRepository, cartVerifier);
    }

    @Test
    void shouldRejectsEmptyCart() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);
        Cart emptyCart = new Cart(CUSTOMER_ID);
        when(cartRepository.findOrCreate(CUSTOMER_ID)).thenReturn(emptyCart);
        StartCheckoutCommand command = new StartCheckoutCommand(CUSTOMER_ID);


        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        assertEquals("Tu carrito está vacío", exception.getMessage());
    }

    @Test
    void shouldReturnsValidSummaryWithTotals() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);

        Cart cart = new Cart(CUSTOMER_ID);
        cart.addProduct(PRODUCT_ID, UUID.randomUUID(), 2); // 2 productos
        when(cartRepository.findOrCreate(CUSTOMER_ID)).thenReturn(cart);

        // Simular que el verificador devuelve todo bien, con un subtotal de 100
        CartVerification.Line line = new CartVerification.Line(
                PRODUCT_ID, UUID.randomUUID(), "Pizza", new BigDecimal("50.00"), 2, new BigDecimal("100.00")
        );

        CartVerification validVerification = new CartVerification(
                List.of(line), List.of(), new BigDecimal("100.00")
        );
        when(cartVerifier.verify(cart)).thenReturn(validVerification);

        StartCheckoutCommand command = new StartCheckoutCommand(CUSTOMER_ID);


        CheckoutSummary summary = useCase.execute(command);

        assertTrue(summary.isValid());
        assertEquals(new BigDecimal("100.00"), summary.subtotal());
        // El costo de envío
        assertEquals(Cart.SHIPPING_COST, summary.shippingCost());
        // Total = 100 + 10 = 110
        assertEquals(new BigDecimal("110.00"), summary.total());
        assertTrue(summary.issues().isEmpty());
    }

    @Test
    void shouldReturnsInvalidSummaryWhenStockIsInsufficient() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);

        Cart cart = new Cart(CUSTOMER_ID);
        cart.addProduct(PRODUCT_ID, UUID.randomUUID(), 11); // Pide 11 pero solo hay 10
        when(cartRepository.findOrCreate(CUSTOMER_ID)).thenReturn(cart);

        // Simulamar que el verificador encontro un problema de stock
        CartVerification.Issue issue = new CartVerification.Issue(
                PRODUCT_ID, "Pizza", CartVerification.IssueType.NOT_AVAILABLE, 11
        );
        CartVerification invalidVerification = new CartVerification(
                List.of(), List.of(issue), BigDecimal.ZERO
        );
        when(cartVerifier.verify(cart)).thenReturn(invalidVerification);

        StartCheckoutCommand command = new StartCheckoutCommand(CUSTOMER_ID);

        CheckoutSummary summary = useCase.execute(command);

        assertFalse(summary.isValid(), "El resumen debería marcarse como inválido");
        assertEquals(1, summary.issues().size(), "Debería reportar el problema de stock");
        assertEquals(CartVerification.IssueType.NOT_AVAILABLE, summary.issues().get(0).type());
    }
}