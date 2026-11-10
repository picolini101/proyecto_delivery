package org.jala.edu.order.domain.model;

import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
public class Order extends BaseModel<OrderId> {
    private final CustomerId customerId;
    private final List<OrderLine> lines;
    private final Money subtotal;
    private OrderStatus status;
    private DeliveryAddress deliveryAddress;
    private Money shippingCost;
    private Money total;

    // Constructor privado para obligar el uso de los Factory Methods
    private Order(OrderId orderId, CustomerId customerId, List<OrderLine> lines, Money subtotal,
                  OrderStatus status, Instant createdAt, Instant updatedAt) {
        super(orderId, createdAt, updatedAt);
        this.customerId = Objects.requireNonNull(customerId, "CustomerId es obligatorio");
        this.lines = new ArrayList<>(Objects.requireNonNull(lines, "Lines es obligatorio"));
        if (lines.isEmpty()) {
            throw new DomainException("No se puede crear un pedido con cero líneas");
        }
        this.subtotal = Objects.requireNonNull(subtotal, "Subtotal es obligatorio");
        this.status = Objects.requireNonNull(status, "OrderStatus es obligatorio");
    }
    /**
     * PATRON FACTORY METHOD APLICADO
     * Establece siempre el estado inicial de un pedido en CREATED, base para que se trabaje en el futuro la maquina de estados.
     */
    public static Order create(CustomerId customerId, List<OrderLine> lines, Money subtotal) {
        Instant now = now();
        return new Order(OrderId.newId(), customerId, lines, subtotal, OrderStatus.CREATED, now, now);
    }

    public static Order restore(OrderId id,
                                CustomerId customerId,
                                List<OrderLine> lines,
                                Money subtotal,
                                OrderStatus status,
                                Instant createdAt,
                                Instant updatedAt) {
        return new Order(id, customerId, lines, subtotal, status, createdAt, updatedAt);
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /** Associates a validated delivery address with this order. */
    public void inputDeliveryAddress(DeliveryAddress address) {
        this.deliveryAddress = Objects.requireNonNull(address, "La dirección de entrega es obligatoria");
        touch();
    }
}
