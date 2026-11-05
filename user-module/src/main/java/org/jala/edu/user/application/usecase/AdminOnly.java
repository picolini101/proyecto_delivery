package org.jala.edu.user.application.usecase;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.user.domain.exception.AdminRequiredException;

/**
 * RF-1.14: the use cases that manage users check the role themselves, not only the menu, so no other path (another
 * module, a test, a future screen) can run them without an administrator signed in.
 */
final class AdminOnly {

    private AdminOnly() {
    }

    /**
     * @throws AdminRequiredException                                  if the signed-in user is not an ADMIN
     * @throws org.jala.edu.core.contract.NotAuthenticatedException if nobody has signed in
     */
    static void check(CurrentUserService currentUserService) {
        if (currentUserService.currentUser().role() != UserRole.ADMIN) {
            throw new AdminRequiredException();
        }
    }
}
