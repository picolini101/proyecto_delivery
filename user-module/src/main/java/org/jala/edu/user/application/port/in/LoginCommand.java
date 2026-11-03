package org.jala.edu.user.application.port.in;

/**
 * Credentials typed on the login screen (D-10).
 */
public record LoginCommand(String email, String password) {

    @Override
    public String toString() {
        return "LoginCommand[email=" + email + ", password=********]";
    }
}
