package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
public class OrderJpaEntity extends BaseJpaEntity {

    @Id
    @Column(name = "order_id")
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column (name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "status")
    private String status;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;
    @Column (name = "total")
    private BigDecimal total;

    @OneToMany(mappedBy = "order")
    private List<ProductLineJpaEntity> products = new ArrayList<>();


    public OrderJpaEntity() {
    }
    public OrderJpaEntity(String entityId, CustomerId customerId, OrderStatus status, DeliveryAddress deliveryAddress, Money shippingCost, Money total, List<ProductLineJpaEntity> listProductEntity) {
    }
}
