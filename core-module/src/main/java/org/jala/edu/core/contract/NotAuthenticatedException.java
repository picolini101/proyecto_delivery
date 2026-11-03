package org.jala.edu.core.contract;

/**
 * Raised by {@link CurrentUserService#currentUser()} when an operation needs a signed-in user and there is none.
 */
public class NotAuthenticatedException extends RuntimeException {

    public NotAuthenticatedException() {
        super("No user has signed in");
    }
}
