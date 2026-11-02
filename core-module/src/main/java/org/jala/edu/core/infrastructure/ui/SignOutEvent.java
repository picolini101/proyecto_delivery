package org.jala.edu.core.infrastructure.ui;

import org.springframework.context.ApplicationEvent;

/**
 * Published when the user chooses "Sign out", before the login screen is shown again. The module that keeps the
 * session listens to it and forgets the current user.
 */
public class SignOutEvent extends ApplicationEvent {

    public SignOutEvent(Object source) {
        super(source);
    }
}
