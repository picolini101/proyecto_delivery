package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;

import java.util.UUID;
@Getter
@Entity
@Table(name = "products")
public class OrderLineJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "product_id")
    private UUID id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column (name = "subtotal")
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderJpaEntity order;

    public OrderLineJpaEntity(ProductId productId, String productName, Money unitPrice, Quantity quantity, Money subtotal, UUID commerceId) {
    }

    public OrderLineJpaEntity() {

    }
}