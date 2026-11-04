package org.jala.edu.user.application.port.in;

import java.util.UUID;

/**
 * A signed-in user changing their own password (#90). Both passwords are plain text as typed; only the new hash is
 * stored.
 */
public record ChangePasswordCommand(UUID userId, String currentPassword, String newPassword) {
}
