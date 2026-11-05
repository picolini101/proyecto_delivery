package org.jala.edu.core.contract;

import java.util.Objects;
import java.util.UUID;

/**
 * A user's public data, as other modules see it (#85): what a screen needs to show or choose a user. Never the user
 * entity. {@code phoneNumber} is {@code null} when the user has none.
 */
public record UserSummary(UUID id, String firstName, String lastName, String email, String phoneNumber,
                          UserRole role, boolean active) {

    public UserSummary {
        Objects.requireNonNull(id, "User id is required");
        Objects.requireNonNull(firstName, "First name is required");
        Objects.requireNonNull(lastName, "Last name is required");
        Objects.requireNonNull(email, "Email is required");
        Objects.requireNonNull(role, "Role is required");
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
