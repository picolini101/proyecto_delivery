package org.jala.edu.catalog.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

/**
 * Comercio del catálogo. Nace Deshabilitado y solo se habilita cuando cumple la regla de
 * negocio de {@code EnableCommerceUseCase} (tener al menos un producto Disponible).
 */
@Getter
@ToString(callSuper = true)
public class Commerce extends BaseModel<CommerceId> {

    private String name;
    private String address;
    private CommercePhone phone;
    private final UUID merchantId;
    private CommerceStatus status;

    private Commerce(
            CommerceId id,
            String name,
            String address,
            String phone,
            UUID merchantId,
            CommerceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = requireText(name, "name");
        this.address = requireText(address, "address");
        this.phone = new CommercePhone(phone);
        this.merchantId = requireMerchant(merchantId);
        this.status = status;
    }

    /** Registra un comercio nuevo del comerciante indicado. Siempre nace Deshabilitado. */
    public static Commerce register(String name, String address, String phone, UUID merchantId) {
        Instant now = now();
        return new Commerce(
                CommerceId.newId(), name, address, phone, merchantId, CommerceStatus.DISABLED, now, now);
    }

    /** Restaura un comercio existente desde la persistencia. */
    public static Commerce restore(
            CommerceId id,
            String name,
            String address,
            String phone,
            UUID merchantId,
            CommerceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        if (status == null) {
            throw new DomainException("Commerce status cannot be null");
        }
        return new Commerce(id, name, address, phone, merchantId, status, createdAt, updatedAt);
    }

    public void updateName(String name) {
        this.name = requireText(name, "name");
        touch();
    }

    public void activate() {
        this.status = CommerceStatus.ACTIVE;
        touch();
    }

    public void disable() {
        this.status = CommerceStatus.DISABLED;
        touch();
    }

    public boolean isActive() {
        return status == CommerceStatus.ACTIVE;
    }

    /** El mensaje nombra el campo erróneo, p. ej. «Commerce address cannot be blank». */
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException("Commerce " + field + " cannot be blank");
        }
        return value.strip();
    }

    private static UUID requireMerchant(UUID merchantId) {
        if (merchantId == null) {
            throw new DomainException("Commerce merchant is required");
        }
        return merchantId;
    }
}