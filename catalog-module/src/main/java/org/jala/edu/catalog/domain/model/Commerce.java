package org.jala.edu.catalog.domain.model;

import java.time.Instant;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class Commerce extends BaseModel<CommerceId> {

    private String name;
    private CommerceStatus status;

    private Commerce(
            CommerceId id,
            String name,
            CommerceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.name = requireText(name);
        this.status = status;
    }

    public static Commerce register(String name) {
        Instant now = now();
        return new Commerce(
                CommerceId.newId(), name, CommerceStatus.ACTIVE, now, now);
    }

    public static Commerce restore(
            CommerceId id,
            String name,
            CommerceStatus status,
            Instant createdAt,
            Instant updatedAt) {
        if (status == null) {
            throw new DomainException("Commerce status cannot be null");
        }
        return new Commerce(id, name, status, createdAt, updatedAt);
    }

    public void updateName(String name) {
        this.name = requireText(name);
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

    private static String requireText(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Commerce name cannot be blank");
        }
        return name.strip();
    }
}
