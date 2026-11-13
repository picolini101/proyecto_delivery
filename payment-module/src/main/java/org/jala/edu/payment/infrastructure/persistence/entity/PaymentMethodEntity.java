package org.jala.edu.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "pay_payment_methods")
public class PaymentMethodEntity {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

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



    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public PaymentMethodEntity() {}

    public PaymentMethodEntity(String id, String userId, String alias, String type, String maskedNumber,
                               boolean isLastRegistered, boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.alias = alias;
        this.type = type;
        this.maskedNumber = maskedNumber;
        this.isLastRegistered = isLastRegistered;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}