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

    private final UUID CUSTOMER_ID = SampleUsers.CUSTOMER;
    private final OrderId ORDER_ID = OrderId.newId();

    @BeforeEach
    void setUp() {
        useCase = new GetOrderDetailUseCase(userService, orderRepository);
    }

    @Test
    void shouldRejectsInactiveCustomer() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(false);
        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(CUSTOMER_ID, ORDER_ID));
    }

    @Test
    void shouldThrowsNotFoundWhenOrderDoesNotExist() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(CUSTOMER_ID, ORDER_ID));
    }

    @Test
    void shouldRejectsAccessToAnotherCustomersOrder() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);

        Order otherOrder = shouldCreateSampleOrder(UUID.randomUUID(), Instant.now());
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(otherOrder));

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(CUSTOMER_ID, ORDER_ID));
    }

    @Test
    void shouldReturnsOrderDetailsWhenValid() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);

        Order myOrder = shouldCreateSampleOrder(CUSTOMER_ID, Instant.now());

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(myOrder));

        Order result = useCase.execute(CUSTOMER_ID, ORDER_ID);

        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(CustomerId.of(CUSTOMER_ID), result.getCustomerId());
    }

    private Order shouldCreateSampleOrder(UUID customerId, Instant date) {
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), UUID.randomUUID(), "Pizza", Money.of(10.0), Quantity.of(1));
        return Order.restore(ORDER_ID, CustomerId.of(customerId), List.of(line), Money.of(10.0), OrderStatus.CONFIRMED, date, date);
    }
}