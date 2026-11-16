package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.dto.OrderTrackingSummary;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

import java.util.UUID;

/**
 * Caso de uso para consultar el estado y la línea de tiempo de un pedido .
 */
public class GetOrderTrackingUseCase {

    private final UserService userService;
    private final OrderRepositoryPort orderRepository;
    private final OrderStatusHistoryPort historyPort;

    public GetOrderTrackingUseCase(UserService userService, OrderRepositoryPort orderRepository, OrderStatusHistoryPort historyPort) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.historyPort = historyPort;
    }

    public OrderTrackingSummary execute(UUID customerId, OrderId orderId) {
        // 1. Validar que el usuario es un cliente activo
        if (!userService.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }

        // 2. Buscar el pedido
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        // 3. Validar que el pedido sea suyo
        if (!order.getCustomerId().equals(CustomerId.of(customerId))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece a este cliente.");
        }

        // 4. Obtener el historial y retornar
        var history = historyPort.findByOrderId(orderId);
        return OrderTrackingSummary.from(order, history);
    }
}