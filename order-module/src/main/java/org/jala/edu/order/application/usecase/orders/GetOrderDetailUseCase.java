package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

import java.util.UUID;

/**
 * Caso de uso para consultar el detalle completo de un pedido (HU-17).
 */
public class GetOrderDetailUseCase {

    private final UserService userService;
    private final OrderRepositoryPort orderRepository;

    public GetOrderDetailUseCase(UserService userService, OrderRepositoryPort orderRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    /**
     * Busca un pedido por ID y valida que pertenezca al cliente autenticado.
     *
     * @param customerId ID del cliente autenticado
     * @param orderId ID del pedido a consultar
     * @return El pedido con todos sus detalles
     * @throws UnauthorizedCustomerException si el usuario no es CUSTOMER o el pedido no es suyo
     * @throws OrderNotFoundException si el pedido no existe
     */
    public Order execute(UUID customerId, OrderId orderId) {
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