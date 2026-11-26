package org.jala.edu.order.application.usecase.orders;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.dto.OrderConfirmationReceipt;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;

public class GetOrderConfirmationUseCase {

    private final OrderRepositoryPort orderRepository;

    public GetOrderConfirmationUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository es obligatorio");
    }

    public OrderConfirmationReceipt execute(UUID orderId, UUID customerId) {
        Objects.requireNonNull(orderId, "orderId es obligatorio");
        Objects.requireNonNull(customerId, "customerId es obligatorio");

        Optional<Order> optionalOrder = orderRepository.findById(OrderId.of(orderId));
        if (optionalOrder.isEmpty()) {
            throw new NotFoundException("Pedido no encontrado: " + orderId);
        }

        Order order = optionalOrder.get();

        if (!order.getCustomerId().equals(CustomerId.of(customerId))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece al cliente solicitante.");
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new DomainException("El pedido aún no ha sido confirmado.");
        }

        return OrderConfirmationReceipt.from(order);
    }
}
