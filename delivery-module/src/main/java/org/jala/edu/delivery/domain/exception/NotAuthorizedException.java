package org.jala.edu.delivery.domain.exception;

import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * The user does not exist, is inactive or does not have the role the operation requires.
 */
public class NotAuthorizedException extends DomainException {

    public NotAuthorizedException(UUID userId) {
        super("User not authorized for this operation: " + userId);
    }
}
