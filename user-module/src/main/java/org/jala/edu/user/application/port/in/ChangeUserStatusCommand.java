package org.jala.edu.user.application.port.in;

import java.util.UUID;
import org.jala.edu.user.domain.model.UserStatus;

/**
 * Activates or deactivates the user with the given id.
 */
public record ChangeUserStatusCommand(UUID id, UserStatus status) {
}
