package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

public class InvalidStateTransitionException extends DomainException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}