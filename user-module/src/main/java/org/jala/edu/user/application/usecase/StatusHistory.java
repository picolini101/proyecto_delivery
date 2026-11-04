package org.jala.edu.user.application.usecase;

import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.domain.model.UserStatusChange;

/**
 * FUN-003 (#82): records who activated or deactivated a user. Shared by the use cases that change a user's status,
 * and called only when the status really changed.
 */
final class StatusHistory {

    private StatusHistory() {
    }

    /**
     * @throws org.jala.edu.core.contract.NotAuthenticatedException if nobody has signed in
     */
    static void record(User user, UserStatus previousStatus, UserStatusHistoryPort history,
                       CurrentUserService currentUserService) {
        CurrentUser author = currentUserService.currentUser();
        history.save(UserStatusChange.record(user.getId(), previousStatus, user.getStatus(), author.id(),
                author.displayName()));
    }
}
