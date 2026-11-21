package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConsultarPedidosUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UserService userService;

    private ConsultarPedidosUseCase useCase;

    private final UUID CUSTOMER_ID = SampleUsers.CUSTOMER;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarPedidosUseCase(userService, orderRepository);
    }

    @Test
    void shouldRejectsInactiveCustomer() {
        Mockito.when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(false);
        Assertions.assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(CUSTOMER_ID));
    }

    @Test
    void shouldReturnsEmptyListWhenNoOrders() {
        Mockito.when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);
        Mockito.when(orderRepository.findByCustomerId(ArgumentMatchers.any())).thenReturn(new ArrayList<>());

        List<Order> result = useCase.execute(CUSTOMER_ID);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnsOrdersSortedByDateDescending() {
        Mockito.when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);

        Order orderOld = shouldCreateSampleOrder(Instant.now().minusSeconds(3600));
        Order orderNew = shouldCreateSampleOrder(Instant.now());

        Mockito.when(orderRepository.findByCustomerId(ArgumentMatchers.any())).thenReturn(List.of(orderOld, orderNew));

        List<Order> result = useCase.execute(CUSTOMER_ID);

        assertEquals(2, result.size());
        assertEquals(orderNew.getId(), result.get(0).getId());
        assertEquals(orderOld.getId(), result.get(1).getId());
    }
    private Order shouldCreateSampleOrder(Instant date) {
        OrderLine line = OrderLine.of(ProductId.of(UUID.randomUUID()), UUID.randomUUID(), "Pizza", Money.of(10.0), Quantity.of(1));

        return Order.restore(OrderId.newId(), CustomerId.of(CUSTOMER_ID), List.of(line), Money.of(10.0), OrderStatus.CONFIRMED, date, date);
    }
}