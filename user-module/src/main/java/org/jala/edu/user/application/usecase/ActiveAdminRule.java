package org.jala.edu.user.application.usecase;

import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.LastActiveAdminException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;

/**
 * FUN-003: the system always keeps at least one active ADMIN. Shared by the use cases that change a user's
 * role or status, and checked before any change is applied.
 */
final class ActiveAdminRule {

    private ActiveAdminRule() {
    }

    /**
     * @throws LastActiveAdminException if {@code user} is the only active ADMIN and would stop being one
     */
    static void check(User user, UserRole newRole, UserStatus newStatus, UserRepositoryPort userRepository) {
        boolean isActiveAdmin = user.isActive() && user.getRole() == UserRole.ADMIN;
        boolean staysActiveAdmin = newRole == UserRole.ADMIN && newStatus == UserStatus.ACTIVE;
        if (isActiveAdmin && !staysActiveAdmin && userRepository.countActiveByRole(UserRole.ADMIN) <= 1) {
            throw new LastActiveAdminException();
        }
    }
}
