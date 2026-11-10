package org.jala.edu.delivery.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * The code typed by the courier is not the one stored for this shipment.
 */
public class InvalidConfirmationCodeException extends DomainException {

    public InvalidConfirmationCodeException() {
        super("The confirmation code does not match");
    }
}
