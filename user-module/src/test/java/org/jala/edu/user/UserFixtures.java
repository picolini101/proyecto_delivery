package org.jala.edu.user;

import java.time.Instant;
import java.util.Optional;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.user.application.port.in.CreateUserCommand;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;

/**
 * Test data for the Arrange step. Builds valid users and commands with sensible defaults,
 * so each test only spells out the values that matter to it.
 */
public final class UserFixtures {

    public static final String FIRST_NAME = "Ana";
    public static final String LAST_NAME = "Perez";
    public static final String PASSWORD = "Secret123";
    public static final String PHONE = "+59170000000";

    /** The sample administrator, as {@code CurrentUserService} returns it after signing in. */
    public static final CurrentUser SIGNED_IN_ADMIN =
            new CurrentUser(SampleUsers.ADMIN, org.jala.edu.core.contract.UserRole.ADMIN, "Sample Admin");

    /** A signed-in customer, to check that administrator-only use cases reject other roles. */
    public static final CurrentUser SIGNED_IN_CUSTOMER =
            new CurrentUser(SampleUsers.CUSTOMER, org.jala.edu.core.contract.UserRole.CUSTOMER, "Sample Customer");

    /** A {@code CurrentUserService} with nobody signed in, e.g. on the sign-up screen. */
    public static final CurrentUserService NOBODY_SIGNED_IN = Optional::empty;

    private UserFixtures() {
    }

    /**
     * A {@code CurrentUserService} whose session belongs to {@code user}.
     */
    public static CurrentUserService signedInAs(CurrentUser user) {
        return () -> Optional.of(user);
    }

    /**
     * A new active customer, created now.
     */
    public static User user(String email) {
        return user(email, UserRole.CUSTOMER);
    }

    /**
     * A new active user with the given role, created now.
     */
    public static User user(String email, UserRole role) {
        return User.register(FIRST_NAME, LAST_NAME, new Email(email), PHONE, role);
    }

    /**
     * An active customer created (and last updated) at the given instant, e.g. to test ordering deterministically.
     */
    public static User userCreatedAt(String email, String createdAt) {
        Instant instant = Instant.parse(createdAt);
        return User.restore(UserId.newId(), FIRST_NAME, LAST_NAME, new Email(email), PHONE,
                UserRole.CUSTOMER, UserStatus.ACTIVE, null, instant, instant);
    }

    public static CreateUserCommand createCommand(String email) {
        return new CreateUserCommand(FIRST_NAME, LAST_NAME, email, PHONE, UserRole.CUSTOMER, PASSWORD);
    }
}
