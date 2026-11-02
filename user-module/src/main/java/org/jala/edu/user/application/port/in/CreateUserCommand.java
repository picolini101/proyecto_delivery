package org.jala.edu.user.application.port.in;

import org.jala.edu.user.domain.model.UserRole;

/**
 * Data to register a user. {@code password} is the plain password typed in the form (D-10); only its hash is stored.
 */
public record CreateUserCommand(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        UserRole role,
        String password) {
}
