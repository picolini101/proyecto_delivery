package org.jala.edu.user.infrastructure.adapter.in.contract;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.ui.SignOutEvent;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.domain.model.User;
import org.springframework.context.event.EventListener;

/**
 * Serves the {@link CurrentUserService} contract (#56) from the desktop session: the login screen signs a user in
 * and "Sign out" ({@link SignOutEvent}) forgets them. It replaces core's fallback.
 * <p>
 * Every lookup reloads the signed-in user (#88, FUN-003): a user the administrator deactivated (or who no longer
 * exists) is signed out at once, and a new role or name applies to the next operation.
 */
public class SessionCurrentUserService implements CurrentUserService {

    private final FindUserUseCase findUserUseCase;
    private final AtomicReference<UUID> signedInId = new AtomicReference<>();

    public SessionCurrentUserService(FindUserUseCase findUserUseCase) {
        this.findUserUseCase = findUserUseCase;
    }

    public void signIn(User user) {
        signedInId.set(user.getId().value());
    }

    @EventListener(SignOutEvent.class)
    public void signOut() {
        signedInId.set(null);
    }

    @Override
    public Optional<CurrentUser> findCurrentUser() {
        UUID id = signedInId.get();
        if (id == null) {
            return Optional.empty();
        }
        Optional<CurrentUser> current = findUserUseCase.execute(id)
                .filter(User::isActive)
                .map(SessionCurrentUserService::toCurrentUser);
        if (current.isEmpty()) {
            signedInId.compareAndSet(id, null);
        }
        return current;
    }

    private static CurrentUser toCurrentUser(User user) {
        return new CurrentUser(user.getId().value(), UserRole.valueOf(user.getRole().name()), user.fullName());
    }
}
