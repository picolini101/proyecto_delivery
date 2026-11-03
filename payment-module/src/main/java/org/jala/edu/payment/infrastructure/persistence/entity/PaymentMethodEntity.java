package org.jala.edu.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "metodos_pago")
public class PaymentMethodEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "alias")
    private String alias;

    @Column(name = "type")
    private String type;

    @Column(name = "masked_number")
    private String maskedNumber;

    @Column(name = "is_last_registered")
    private boolean isLastRegistered;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PaymentMethodEntity() {}

    public PaymentMethodEntity(UUID id, UUID userId, String alias, String type, String maskedNumber, boolean isLastRegistered, boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.alias = alias;
        this.type = type;
        this.maskedNumber = maskedNumber;
        this.isLastRegistered = isLastRegistered;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getAlias() { return alias; }
    public String getType() { return type; }
    public String getMaskedNumber() { return maskedNumber; }
    public boolean isLastRegistered() { return isLastRegistered; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}