package org.jala.edu.user.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Wrong email or password. The message never says which one failed.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
