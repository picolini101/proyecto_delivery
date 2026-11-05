package org.jala.edu.core.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Identity check and user lookup offered by {@code user-module} to every other module.
 */
public interface UserService {

    /**
     * @return {@code true} only if the user exists, is active and has at least one of the given roles
     */
    boolean isActiveWithRole(UUID userId, UserRole... roles);

    /**
     * @return the user's public data whatever their status, or empty when the id is unknown
     */
    Optional<UserSummary> findUser(UUID userId);

    /**
     * @return the active users with the given role, sorted by full name (e.g. to choose the merchant of a commerce)
     */
    List<UserSummary> findActiveByRole(UserRole role);
}
