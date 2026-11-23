package org.jala.edu.order.domain.model;

import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;

import java.time.Instant;
import java.util.*;


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
    private UUID courierId;

    // Constructor privado para obligar el uso de los Factory Methods
    private Order(OrderId orderId, CustomerId customerId, List<OrderLine> lines, Money subtotal,
                  OrderStatus status, Instant createdAt, Instant updatedAt) {
        super(orderId, createdAt, updatedAt);
        this.customerId = Objects.requireNonNull(customerId, "CustomerId es obligatorio");
        Objects.requireNonNull(lines, "Lines es obligatorio");
        this.lines = new ArrayList<>(lines.size());
        for (OrderLine line : lines) {
            this.lines.add(Objects.requireNonNull(line, "OrderLine es obligatoria").withOrderId(orderId));
        }
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

    // Reconstruye el objeto desde la base de datos: necesita todos los campos guardados.
    @SuppressWarnings("checkstyle:ParameterNumber")
    public static Order restore(OrderId id, CustomerId customerId, List<OrderLine> lines, Money subtotal,
                                OrderStatus status, DeliveryAddress deliveryAddress, Money shippingCost,
                                Money total, Instant createdAt, Instant updatedAt) {
        Order order = new Order(id, customerId, lines, subtotal, status, createdAt, updatedAt);
        order.deliveryAddress = deliveryAddress;
        order.shippingCost = shippingCost;
        order.total = total;
        return order;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /** Associates a validated delivery address with this order. */
    public void inputDeliveryAddress(DeliveryAddress address) {
        this.deliveryAddress = Objects.requireNonNull(address, "La dirección de entrega es obligatoria");
        touch();
    }

    public void transitionTo(OrderStatus newStatus, String reason) {
        if (this.status.isFinal()) {
            throw new InvalidStateTransitionException(
                    "No se puede cambiar el estado de un pedido finalizado (" + this.status + ")."
            );
        }
        // Criterio 2: Si la transición no está permitida por la tabla, rechazar
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                    "Transición inválida: no se permite pasar de " + this.status + " a " + newStatus + "."
            );
        }
        if (newStatus == OrderStatus.CANCELLED) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new DomainException("El motivo de cancelación es obligatorio.");
            }
        }
        this.status = newStatus;
        touch();
    }
    public void assignCourier(UUID courierId) {
        if (this.courierId != null) {
            throw new IllegalStateException(
                    "This order already has courier ");
        }
        this.courierId = Objects.requireNonNull(courierId, "CourierId is neccesary");
        touch();
    }
}
