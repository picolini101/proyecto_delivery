package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;
import java.time.Instant;
import java.util.UUID;
@Getter
@Entity
@Table(name = "order_line")
public class OrderLineJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "order_line_id", nullable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "commerce_id", nullable = false)
    private UUID commerceId;

    @Column (name = "subtotal")
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    public OrderLineJpaEntity(UUID id,
                              UUID productId,
                              String productName,
                              Double unitPrice,
                              Integer quantity,
                              UUID commerceId,
                              Double subtotal,
                              OrderJpaEntity order,
                              Instant createdAt,
                              Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.commerceId = commerceId;
        this.subtotal = subtotal;
        this.order = order;
    }
    public OrderLineJpaEntity() {

    }

    void setOrder(OrderJpaEntity order) {
        this.order = order;
    }

}
