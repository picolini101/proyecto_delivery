package org.jala.edu.user.infrastructure.adapter.in.contract;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
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
 * exists) is signed out at once, and a new role or name applies to the next operation. The session also ends when no
 * lookup happened for longer than the idle timeout (RF-1.15, {@code delivery.session.idle-timeout}).
 */
public class SessionCurrentUserService implements CurrentUserService {

    private final FindUserUseCase findUserUseCase;
    private final Duration idleTimeout;
    private final Clock clock;
    private final AtomicReference<UUID> signedInId = new AtomicReference<>();
    private volatile Instant lastActivity;

    /**
     * A session that never expires by itself, e.g. for tests.
     */
    public SessionCurrentUserService(FindUserUseCase findUserUseCase) {
        this(findUserUseCase, Duration.ZERO, Clock.systemUTC());
    }

    /**
     * @param idleTimeout how long the session survives without lookups; {@link Duration#ZERO} never expires
     */
    public SessionCurrentUserService(FindUserUseCase findUserUseCase, Duration idleTimeout, Clock clock) {
        this.findUserUseCase = findUserUseCase;
        this.idleTimeout = idleTimeout;
        this.clock = clock;
    }

    public void signIn(User user) {
        lastActivity = clock.instant();
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
        Instant now = clock.instant();
        if (!idleTimeout.isZero() && lastActivity.plus(idleTimeout).isBefore(now)) {
            signedInId.compareAndSet(id, null);
            return Optional.empty();
        }
        lastActivity = now;
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
