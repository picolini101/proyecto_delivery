package org.jala.edu.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jala.edu.order.domain.model.Order;

public record OrderSummary(
        UUID orderId,
        String shortId,
        UUID customerId,
        int totalItems,
        BigDecimal subtotal,
        String status,
        Instant createdAt
) {
    public static OrderSummary from(Order order) {
        int itemsCount = order.getLines().stream()
                .mapToInt(line -> line.getQuantity().value())
                .sum();
        String shortId = order.getId().value().toString().substring(0, 8).toUpperCase();
        return new OrderSummary(
                order.getId().value(),
                shortId,
                order.getCustomerId().value(),
                itemsCount,
                order.getSubtotal().value(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}
