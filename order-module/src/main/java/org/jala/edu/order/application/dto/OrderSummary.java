package org.jala.edu.order.application.dto;

import org.jala.edu.order.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jala.edu.order.domain.model.Order;

public record OrderSummary(
        UUID orderId,
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
        return new OrderSummary(
                order.getId().value(),
                order.getCustomerId().value(),
                itemsCount,
                order.getSubtotal().value(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}
