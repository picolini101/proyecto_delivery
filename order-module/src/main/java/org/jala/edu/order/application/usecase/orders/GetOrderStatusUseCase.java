package org.jala.edu.order.application.usecase.orders;

import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;


public class GetOrderStatusUseCase {

    private final UserService userService;
    private final OrderRepositoryPort orderRepository;

    public GetOrderStatusUseCase(UserService userService, OrderRepositoryPort orderRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    /**
     * Lista los pedidos del cliente.
     */
    public List<Order> execute(UUID customerId) {
        if (!userService.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }
        return orderRepository.findByCustomerId(CustomerId.of(customerId));
    }

    /**
     * Obtiene el detalle de un pedido validando que sea del cliente.
     */
    public Order getOrderDetail(UUID customerId, OrderId orderId) {
        if (!userService.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        if (!order.getCustomerId().equals(CustomerId.of(customerId))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece a este cliente.");
        }

        return order;
    }
}