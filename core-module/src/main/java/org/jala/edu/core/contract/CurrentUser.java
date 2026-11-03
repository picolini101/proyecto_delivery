package org.jala.edu.core.contract;

import java.util.Objects;
import java.util.UUID;

/**
 * The person using the application, as other modules see them: id, role and a name to show. Never the user entity.
 */
public record CurrentUser(UUID id, UserRole role, String displayName) {

    public CurrentUser {
        Objects.requireNonNull(id, "User id is required");
        Objects.requireNonNull(role, "Role is required");
        Objects.requireNonNull(displayName, "Display name is required");
    }
}
