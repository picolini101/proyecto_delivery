package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "order_id")
    private Integer id;

    private BigDecimal subtotal;

    private String status;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;

    private BigDecimal total;

    @OneToMany(mappedBy = "order")
    private List<ProductJpaEntity> products = new ArrayList<>();
}
