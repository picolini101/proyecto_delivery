package org.jala.edu.order.application.dto;

import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;

import java.util.List;
import java.util.UUID;

/**
 * DTO con la información necesaria para pintar la línea de tiempo del pedido.
 */
public record OrderTrackingSummary(
        UUID orderId,
        String shortId,
        String productName,
        OrderStatus currentStatus,
        List<OrderStatusChange> history
) {
    public static OrderTrackingSummary from(Order order, List<OrderStatusChange> history) {
        String orderIdStr = order.getId().value().toString();
        // los ultimos caracteres del ID del producto
        String shortId = orderIdStr.substring(Math.max(0, orderIdStr.length() - 4)).toUpperCase();

        String productName = order.getLines().isEmpty()
                ? "Pedido"
                : order.getLines().get(0).productName();

        return new OrderTrackingSummary(
                order.getId().value(),
                shortId,
                productName,
                order.getStatus(),
                history
        );
    }
}