package org.jala.edu.user.domain.model;

import java.util.Objects;
import java.util.UUID;

public record UserStatusChangeId(UUID value) {

    public UserStatusChangeId {
        Objects.requireNonNull(value, "Status change id cannot be null");
    }

    public static UserStatusChangeId newId() {
        return new UserStatusChangeId(UUID.randomUUID());
    }

    public static UserStatusChangeId of(String value) {
        return new UserStatusChangeId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
