package org.jala.edu.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;

/**
 * DTO inmutable de salida que contiene los datos del comprobante de un pedido confirmado (HU-12).
 */
public record OrderConfirmationReceipt(
        UUID orderId,
        String deliveryCode,
        String status,
        Instant confirmedAt,
        String deliveryAddress,
        List<LineReceipt> lines,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal total
) {

    public record LineReceipt(
            UUID productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {
        public static LineReceipt from(OrderLine line) {
            return new LineReceipt(
                    line.getProductId().value(),
                    line.getProductName(),
                    line.getUnitPrice().value(),
                    line.getQuantity().value(),
                    line.getSubtotal().value()
            );
        }
    }

    public static OrderConfirmationReceipt from(Order order) {
        List<LineReceipt> receiptLines = new ArrayList<>();
        for (OrderLine line : order.getLines()) {
            receiptLines.add(LineReceipt.from(line));
        }

        String addressStr = order.getDeliveryAddress() != null
                ? order.getDeliveryAddress().toString()
                : "Sin dirección registrada";

        BigDecimal shipping = order.getShippingCost() != null
                ? order.getShippingCost().value()
                : BigDecimal.ZERO;

        BigDecimal orderTotal = order.getTotal() != null
                ? order.getTotal().value()
                : order.getSubtotal().value().add(shipping);

        return new OrderConfirmationReceipt(
                order.getId().value(),
                order.getDeliveryCode(),
                order.getStatus().name(),
                order.getUpdatedAt(),
                addressStr,
                Collections.unmodifiableList(receiptLines),
                order.getSubtotal().value(),
                shipping,
                orderTotal
        );
    }
}