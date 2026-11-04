package org.jala.edu.user.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Raised when an operation reserved for administrators is attempted by another role.
 */
public class AdminRequiredException extends DomainException {

    public AdminRequiredException() {
        super("Only an administrator can do this");
    }
}
