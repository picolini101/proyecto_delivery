package org.jala.edu.user.application.port.in;

import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;

/**
 * Input of the "list users" use case (FUN-004, RF-1.09). {@code emailFilter} keeps only the users whose email contains
 * it, ignoring case; {@code role} and {@code status} keep only that role or status. A {@code null} (or blank) filter
 * does not filter.
 */
public record ListUsersQuery(String emailFilter, UserRole role, UserStatus status) {

    /**
     * Lists every user.
     */
    public static ListUsersQuery all() {
        return new ListUsersQuery(null, null, null);
    }

    /**
     * Lists the users whose email contains {@code emailFilter}, whatever their role or status.
     */
    public static ListUsersQuery byEmail(String emailFilter) {
        return new ListUsersQuery(emailFilter, null, null);
    }
}
