package org.jala.edu.user.domain.exception;

import org.jala.edu.core.domain.exception.ConflictException;

/**
 * Raised when a change would leave the system without an active administrator (FUN-003).
 */
public class LastActiveAdminException extends ConflictException {

    public LastActiveAdminException() {
        super("There must be at least one active administrator");
    }
}
