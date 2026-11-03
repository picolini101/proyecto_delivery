package org.jala.edu.user.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Raised when an inactive user tries to sign in (FUN-003).
 */
public class InactiveUserException extends DomainException {

    public InactiveUserException() {
        super("Inactive user");
    }
}
