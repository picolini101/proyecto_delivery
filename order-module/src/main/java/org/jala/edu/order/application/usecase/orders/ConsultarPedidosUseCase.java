package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para listar el historial de pedidos de un cliente
 */
public class ConsultarPedidosUseCase {

    private final UserService userService;
    private final OrderRepositoryPort orderRepository;

    public ConsultarPedidosUseCase(UserService userService, OrderRepositoryPort orderRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    /**
     * Obtiene los pedidos del cliente autenticado, ordenados del más reciente al más antiguo.
     *
     * @param customerId ID del cliente autenticado
     * @return Lista de pedidos ordenada
     * @throws UnauthorizedCustomerException si el usuario no es un CUSTOMER activo
     */
    public List<Order> execute(UUID customerId) {
        if (!userService.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }

        List<Order> orders = orderRepository.findByCustomerId(CustomerId.of(customerId));
        List<Order> sortedOrders = new ArrayList<>(orders);
        sortedOrders.sort(Comparator.comparing(Order::getCreatedAt).reversed());

        return sortedOrders;
    }
}