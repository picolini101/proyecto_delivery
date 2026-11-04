package org.jala.edu.user.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * The current password typed to change it (#90) does not match the stored one.
 */
public class WrongCurrentPasswordException extends DomainException {

    public WrongCurrentPasswordException() {
        super("Current password is incorrect");
    }
}
