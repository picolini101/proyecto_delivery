package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetOrderDetailUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UserService userService;

    private GetOrderDetailUseCase useCase;

    private final UUID customerId = SampleUsers.CUSTOMER;
    private final OrderId orderId = OrderId.newId();



    @BeforeEach
    void setUp() {
        useCase = new GetOrderDetailUseCase(userService, orderRepository);
    }

    @Test
    void shouldRejectsInactiveCustomer() {
        when(userService.isActiveWithRole(customerId, UserRole.CUSTOMER)).thenReturn(false);
        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(customerId, orderId));
    }

    @Test
    void shouldThrowsNotFoundWhenOrderDoesNotExist() {
        when(userService.isActiveWithRole(customerId, UserRole.CUSTOMER)).thenReturn(true);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(customerId, orderId));
    }

    @Test
    void shouldRejectsAccessToAnotherCustomersOrder() {
        when(userService.isActiveWithRole(customerId, UserRole.CUSTOMER)).thenReturn(true);

        Order otherOrder = shouldCreateSampleOrder(UUID.randomUUID(), Instant.now());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(otherOrder));

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(customerId, orderId));
    }

    @Test
    void shouldReturnsOrderDetailsWhenValid() {
        when(userService.isActiveWithRole(customerId, UserRole.CUSTOMER)).thenReturn(true);

        Order myOrder = shouldCreateSampleOrder(customerId, Instant.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));

        Order result = useCase.execute(customerId, orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(CustomerId.of(customerId), result.getCustomerId());
    }

    private Order shouldCreateSampleOrder(UUID customerId, Instant date) {
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), UUID.randomUUID(), "Pizza", Money.of(10.0), Quantity.of(1));
        return Order.restore(orderId, CustomerId.of(customerId), List.of(line), Money.of(10.0), OrderStatus.CONFIRMED, date, date);
    }
}