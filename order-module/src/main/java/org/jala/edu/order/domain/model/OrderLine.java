package org.jala.edu.order.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.model.BaseModel;

/** A product snapshot included in an order. */
@Getter
@ToString(callSuper = true)
public class OrderLine extends BaseModel<OrderLineId> {

    private final ProductId productId;
    private final UUID commerceId;
    private final String productName;
    private final Money unitPrice;
    private final Quantity quantity;
    private final Money subtotal;
    private final OrderId orderId;

    private OrderLine(OrderLineId id, ProductId productId, UUID commerceId, String productName,
                      Money unitPrice, Quantity quantity, Money subtotal,
                      Instant createdAt, Instant updatedAt, OrderId orderId) {
        super(id, createdAt, updatedAt);
        this.productId = Objects.requireNonNull(productId, "ProductId no puede ser nulo");
        this.commerceId = Objects.requireNonNull(commerceId, "CommerceId no puede ser nulo");
        this.productName = Objects.requireNonNull(productName, "ProductName no puede ser nulo");
        this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice no puede ser nulo");
        this.quantity = Objects.requireNonNull(quantity, "Quantity no puede ser nula");
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal no puede ser nulo");
        this.orderId = orderId;
    }

    public static OrderLine of(ProductId productId, UUID commerceId, String productName,
                               Money unitPrice, Quantity quantity, OrderId orderId) {
        Instant now = now();
        return new OrderLine(OrderLineId.newId(), productId, commerceId, productName, unitPrice,
                quantity, unitPrice.multiply(quantity), now, now, orderId);
    }

    public static OrderLine restore(OrderLineId id, ProductId productId, UUID commerceId, String productName,
                                    Money unitPrice, Quantity quantity, Money subtotal, OrderId orderId,
                                    Instant createdAt, Instant updatedAt) {
        return new OrderLine(id, productId, commerceId, productName, unitPrice, quantity,
                subtotal, createdAt, updatedAt,orderId );
    }
}
