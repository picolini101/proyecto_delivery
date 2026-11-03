package org.jala.edu.user.application.port.in;

/**
 * Input of the "list users" use case (FUN-004). {@code emailFilter} keeps only the users whose email contains it,
 * ignoring case; {@code null} or blank lists every user.
 */
public record ListUsersQuery(String emailFilter) {

    /**
     * Lists every user.
     */
    public static ListUsersQuery all() {
        return new ListUsersQuery(null);
    }
}
