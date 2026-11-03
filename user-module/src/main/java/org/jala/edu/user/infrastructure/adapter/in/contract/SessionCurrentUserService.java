package org.jala.edu.user.infrastructure.adapter.in.contract;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.ui.SignOutEvent;
import org.jala.edu.user.domain.model.User;
import org.springframework.context.event.EventListener;

/**
 * Serves the {@link CurrentUserService} contract (#56) from the desktop session: the login screen signs a user in
 * and "Sign out" ({@link SignOutEvent}) forgets them. It replaces core's fallback.
 */
public class SessionCurrentUserService implements CurrentUserService {

    private final AtomicReference<CurrentUser> current = new AtomicReference<>();

    public void signIn(User user) {
        current.set(new CurrentUser(user.getId().value(), UserRole.valueOf(user.getRole().name()), user.fullName()));
    }

    @EventListener(SignOutEvent.class)
    public void signOut() {
        current.set(null);
    }

    @Override
    public Optional<CurrentUser> findCurrentUser() {
        return Optional.ofNullable(current.get());
    }
}
