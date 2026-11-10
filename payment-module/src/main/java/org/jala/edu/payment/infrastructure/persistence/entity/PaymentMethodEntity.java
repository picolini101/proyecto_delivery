package org.jala.edu.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pay_payment_methods")
public class PaymentMethodEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "alias", nullable = false)
    private String alias;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "masked_number", nullable = false)
    private String maskedNumber;

    @Column(name = "is_last_registered", nullable = false)
    private boolean isLastRegistered;

    @Column(name = "is_active", nullable = false)
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
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMaskedNumber() { return maskedNumber; }
    public void setMaskedNumber(String maskedNumber) { this.maskedNumber = maskedNumber; }

    public boolean isLastRegistered() { return isLastRegistered; }
    public void setLastRegistered(boolean lastRegistered) { this.isLastRegistered = lastRegistered; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}