package org.jala.edu.order.application.usecase.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Fase RED: Escribimos las pruebas ANTES de crear GetOrderStatusUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetOrderStatusUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UserService userService;

    private GetOrderStatusUseCase useCase;

    private final UUID CUSTOMER_ID = SampleUsers.CUSTOMER;
    private final OrderId ORDER_ID = OrderId.newId();

    @BeforeEach
    void setUp() {
        useCase = new GetOrderStatusUseCase(userService, orderRepository);
    }

    /** Criterio 1: Un cliente inactivo no puede ver sus pedidos. */
    @Test
    void rejectsInactiveCustomer() {
        // Arrange
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedCustomerException.class, () -> useCase.execute(CUSTOMER_ID));
    }

    /** Criterio 2: El cliente solo ve SUS pedidos. */
    @Test
    void returnsOnlyCustomerOrders() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);
        Order order = createSampleOrder(CUSTOMER_ID);
        when(orderRepository.findByCustomerId(CustomerId.of(CUSTOMER_ID))).thenReturn(List.of(order));

        List<Order> result = useCase.execute(CUSTOMER_ID);

        assertEquals(1, result.size());
        assertEquals(CUSTOMER_ID, result.get(0).getCustomerId().value());
    }

    /** Criterio 3: Consultar el detalle de un pedido ajeno lanza excepción de seguridad. */
    @Test
    void rejectsConsultingAnotherCustomersOrder() {
        when(userService.isActiveWithRole(CUSTOMER_ID, UserRole.CUSTOMER)).thenReturn(true);
        UUID otherCustomer = UUID.randomUUID();
        Order otherOrder = createSampleOrder(otherCustomer);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(otherOrder));

        assertThrows(UnauthorizedCustomerException.class, () -> useCase.getOrderDetail(CUSTOMER_ID, ORDER_ID));
    }

    private Order createSampleOrder(UUID customerId) {
        return Order.restore(ORDER_ID, CustomerId.of(customerId),
                List.of(OrderLine.of(ProductId.of(UUID.randomUUID()), "Pizza", Money.of(10.0), Quantity.of(1))),
                Money.of(10.0), OrderStatus.CREATED, java.time.Instant.now(), java.time.Instant.now());
    }
}