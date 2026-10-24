package org.jala.edu.delivery.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * The acting user is missing, inactive or does not have the role the operation requires.
 */
public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException() {
        super("User not authorized for this operation");
    }
}
