package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductJpaEntity {

    @Id
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "unit_price")
    private Double unitPrice;

    private Integer quantity;

    @Column(name = "merchant_id")
    private Integer merchantId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderJpaEntity order;
}