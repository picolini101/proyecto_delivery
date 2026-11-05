package org.jala.edu.user.application.port.in;

/**
 * The signed-in user's own contact data (RF-1.08). The email and the role identify the account, so only an
 * administrator changes them. {@code phoneNumber} is optional.
 */
public record UpdateMyProfileCommand(String firstName, String lastName, String phoneNumber) {
}
