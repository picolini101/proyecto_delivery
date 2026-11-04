package org.jala.edu.payment.application.dto;

import java.util.Objects;
import java.util.UUID;

public class RegisterPaymentMethodCommand {

    private final UUID userId;
    private final String alias;
    private final String type;
    private final String rawNumber;



    public RegisterPaymentMethodCommand(UUID userId, String alias, String type, String rawNumber) {
        this.userId = Objects.requireNonNull(userId, "El userId no puede ser nulo");
        this.alias = alias;
        this.type = type;
        this.rawNumber = rawNumber;
    }

    public UUID getUserId() { return userId; }
    public String getAlias() { return alias; }
    public String getType() { return type; }
    public String getRawNumber() { return rawNumber; }
}