package org.jala.edu.payment.domain.model;

import java.util.Objects;
import java.util.UUID;

public class PaymentMethod {

    private final UUID id;
    private final UUID userId;
    private Alias alias;
    private final PaymentType type;
    private String maskedNumber;
    private boolean isLastRegistered;
    private boolean active;

    public PaymentMethod(UUID id, UUID userId, Alias alias, PaymentType type, String maskedNumber,
                         boolean isLastRegistered, boolean active) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.userId = Objects.requireNonNull(userId, "El userId no puede ser nulo");
        this.alias = Objects.requireNonNull(alias, "El alias no puede ser nulo");
        this.type = Objects.requireNonNull(type, "El tipo de pago no puede ser nulo");
        this.maskedNumber = maskedNumber;
        this.isLastRegistered = isLastRegistered;
        this.active = active;
    }

    public static PaymentMethod createNew(UUID userId, Alias alias, PaymentType type, String rawNumber) {
        String masked = maskNumber(type, rawNumber);
        return new PaymentMethod(
                UUID.randomUUID(),
                userId,
                alias,
                type,
                masked,
                true,
                true
        );
    }

    private static String maskNumber(PaymentType type, String rawNumber) {
        if (rawNumber == null || rawNumber.isBlank()) {
            return "**** **** **** ****";
        }
        String clean = rawNumber.replaceAll("\\s+", "");
        if (clean.length() <= 4) {
            return "**** " + clean;
        }
        return "**** **** **** " + clean.substring(clean.length() - 4);
    }

    public void markAsLastRegistered() { this.isLastRegistered = true; }
    public void unmarkAsLastRegistered() { this.isLastRegistered = false; }
    public void deactivate() { this.active = false; this.isLastRegistered = false; }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Alias getAlias() { return alias; }
    public PaymentType getType() { return type; }
    public String getMaskedNumber() { return maskedNumber; }
    public boolean isLastRegistered() { return isLastRegistered; }
    public boolean isActive() { return active; }
}