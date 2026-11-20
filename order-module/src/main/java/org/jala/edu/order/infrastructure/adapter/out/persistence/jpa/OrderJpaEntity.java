package org.jala.edu.order.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;

import java.math.BigDecimal;
import java.time.Instant;
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

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_avenue")
    private String deliveryAvenue;

    @Column(name = "delivery_house_number")
    private String deliveryHouseNumber;

    @Column(name = "shipping_cost")
    private BigDecimal shippingCost;
    @Column (name = "total")
    private BigDecimal total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineJpaEntity> products = new ArrayList<>();


    public OrderJpaEntity() {
    }

    public OrderJpaEntity(UUID id, UUID customerId, BigDecimal subtotal, String status,
                          String deliveryCity, String deliveryAvenue, String deliveryHouseNumber,
                          BigDecimal shippingCost, BigDecimal total, List<OrderLineJpaEntity> products,
                          Instant createdAt, Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.status = status;
        this.deliveryCity = deliveryCity;
        this.deliveryAvenue = deliveryAvenue;
        this.deliveryHouseNumber = deliveryHouseNumber;
        this.shippingCost = shippingCost;
        this.total = total;
        this.products = new ArrayList<>();
        setProducts(products);
    }

    void setProducts(List<OrderLineJpaEntity> products) {
        this.products.clear();
        if (products != null) {
            for (OrderLineJpaEntity product : products) {
                product.setOrder(this);
                this.products.add(product);
            }
        }
    }

}
