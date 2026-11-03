package org.jala.edu.core.contract;

import java.util.Optional;

/**
 * Who is using the application, offered by {@code user-module} (login) to every other module.
 * Use it to know which user performs an operation; keep checking permissions with {@link UserService#isActiveWithRole}.
 */
public interface CurrentUserService {

    /**
     * @return the signed-in user, or empty when nobody has signed in
     */
    Optional<CurrentUser> findCurrentUser();

    /**
     * @return the signed-in user
     * @throws NotAuthenticatedException when nobody has signed in
     */
    default CurrentUser currentUser() {
        return findCurrentUser().orElseThrow(NotAuthenticatedException::new);
    }
}
