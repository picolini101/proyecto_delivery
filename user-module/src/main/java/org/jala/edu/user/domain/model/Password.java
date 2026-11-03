package org.jala.edu.user.domain.model;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * A password as typed by the user (D-10). It only lives long enough to be hashed: it is never stored and
 * {@link #toString()} hides it.
 */
public record Password(String value) {

    public static final int MIN_LENGTH = 8;

    public Password {
        if (value == null || value.length() < MIN_LENGTH) {
            throw new DomainException("Password must have at least " + MIN_LENGTH + " characters");
        }
    }

    @Override
    public String toString() {
        return "********";
    }
}
