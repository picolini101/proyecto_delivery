package org.jala.edu.delivery.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

@Getter
@ToString(callSuper = true)
public class Courier extends BaseModel<UUID> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?[0-9]{7,15}");

    private final String name;
    private final String phoneNumber;
    private final TransportMode transportMode;
    private final UserRole role;

    private Courier(UUID id, String name, String phoneNumber, TransportMode transportMode,
                    Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);

        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre es obligatorio");
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            throw new DomainException("El teléfono debe contener entre 7 y 15 dígitos");
        }

        if (transportMode == null) {
            throw new DomainException("Selecciona un medio de transporte");
        }

        this.name = name.strip();
        this.phoneNumber = phoneNumber.strip();
        this.transportMode = transportMode;
        this.role = UserRole.COURIER;
    }

    public static Courier register(String name, String phoneNumber, TransportMode transportMode) {
        Instant now = now();

        return new Courier(
                UUID.randomUUID(),
                name,
                phoneNumber,
                transportMode,
                now,
                now
        );
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber.strip()).matches();
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public TransportMode getTransportMode() {
        return transportMode;
    }

    public UserRole getRole() {
        return role;
    }
}