package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ListConfirmedOrdersByMerchantUseCaseTest {

    private final OrderRepositoryPort orderRepository = mock(OrderRepositoryPort.class);
    private final UserService userService = mock(UserService.class);
    private final UUID merchantId = UUID.randomUUID();
    private ListConfirmedOrdersByMerchantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListConfirmedOrdersByMerchantUseCase(orderRepository, userService);
    }

    @Test
    void returnsOnlyConfirmedOrdersContainingProductsOfTheMerchant() {
        Order merchantConfirmed = order(merchantId, OrderStatus.CONFIRMED);
        Order merchantCreated = order(merchantId, OrderStatus.CREATED);
        Order anotherMerchantConfirmed = order(UUID.randomUUID(), OrderStatus.CONFIRMED);
        when(userService.isActiveWithRole(merchantId, UserRole.MERCHANT)).thenReturn(true);
        when(orderRepository.findAll()).thenReturn(List.of(
                merchantConfirmed, merchantCreated, anotherMerchantConfirmed));

        List<Order> result = useCase.execute(merchantId);

        assertEquals(List.of(merchantConfirmed), result);
    }

    @Test
    void rejectsUserThatIsNotAnActiveMerchant() {
        when(userService.isActiveWithRole(merchantId, UserRole.MERCHANT)).thenReturn(false);

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(merchantId));

        verifyNoInteractions(orderRepository);
    }

    private Order order(UUID commerceId, OrderStatus status) {
        Instant now = Instant.now();
        OrderId id = OrderId.newId();
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), commerceId, "Product", Money.of(10.0),
                Quantity.of(1));
        return Order.restore(id, CustomerId.of(UUID.randomUUID()), List.of(line), Money.of(10.0),
                status, now, now);
    }
}
