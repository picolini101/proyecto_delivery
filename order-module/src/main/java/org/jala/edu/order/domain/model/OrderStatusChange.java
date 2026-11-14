package org.jala.edu.order.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.jala.edu.core.domain.model.BaseModel;

@Getter
public class OrderStatusChange extends BaseModel<OrderStatusChangeId> {

    private final OrderId orderId;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final UUID changedById;
    private final String reason;

    private OrderStatusChange(OrderStatusChangeId id, OrderId orderId, OrderStatus previousStatus,
                              OrderStatus newStatus, UUID changedById, String reason,
                              Instant createdAt) {
        super(id, createdAt, createdAt);
        this.orderId = Objects.requireNonNull(orderId, "OrderId es obligatorio");
        this.previousStatus = Objects.requireNonNull(previousStatus, "Estado previo es obligatorio");
        this.newStatus = Objects.requireNonNull(newStatus, "Nuevo estado es obligatorio");
        this.changedById = Objects.requireNonNull(changedById, "Id del usuario responsable es obligatorio");
        this.reason = reason;
    }

    public static OrderStatusChange record(OrderId orderId, OrderStatus previousStatus,
                                           OrderStatus newStatus, UUID changedById, String reason) {
        return new OrderStatusChange(
                OrderStatusChangeId.newId(),
                orderId,
                previousStatus,
                newStatus,
                changedById,
                reason,
                now()
        );
    }
}