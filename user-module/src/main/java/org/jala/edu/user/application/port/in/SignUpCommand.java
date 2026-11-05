package org.jala.edu.user.application.port.in;

/**
 * Data a new customer types to create their own account (#89). {@code password} is the plain password; only its hash
 * is stored. {@code phoneNumber} is optional.
 */
public record SignUpCommand(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String password) {
}
