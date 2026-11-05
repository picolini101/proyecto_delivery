package org.jala.edu.user.infrastructure.adapter.in.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jala.edu.user.UserFixtures.user;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.NotAuthenticatedException;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;

class SessionCurrentUserServiceTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final SessionCurrentUserService session = new SessionCurrentUserService(new FindUserUseCase(repository));

    @Test
    void nobodyIsSignedInAtStartup() {
        // Arrange (new session)

        // Act + Assert
        assertThat(session.findCurrentUser()).isEmpty();
        assertThatThrownBy(session::currentUser).isInstanceOf(NotAuthenticatedException.class);
    }

    @Test
    void signInExposesIdRoleAndNameToOtherModules() {
        // Arrange
        User courier = repository.save(user("ana@example.com", UserRole.COURIER));

        // Act
        session.signIn(courier);

        // Assert
        CurrentUser current = session.currentUser();
        assertThat(current.id()).isEqualTo(courier.getId().value());
        assertThat(current.role()).isEqualTo(org.jala.edu.core.contract.UserRole.COURIER);
        assertThat(current.displayName()).isEqualTo("Ana Perez");
    }

    @Test
    void signOutForgetsTheUser() {
        // Arrange
        session.signIn(repository.save(user("ana@example.com")));

        // Act
        session.signOut();

        // Assert
        assertThat(session.findCurrentUser()).isEmpty();
    }

    @Test
    void aUserDeactivatedDuringTheSessionCanNoLongerOperate() {
        // Arrange
        User ana = repository.save(user("ana@example.com"));
        session.signIn(ana);

        // Act
        ana.deactivate();
        repository.save(ana);

        // Assert
        assertThat(session.findCurrentUser()).isEmpty();
        assertThatThrownBy(session::currentUser).isInstanceOf(NotAuthenticatedException.class);
    }

    @Test
    void aDeactivatedUserStaysSignedOutAfterBeingActivatedAgain() {
        // Arrange
        User ana = repository.save(user("ana@example.com"));
        session.signIn(ana);
        ana.deactivate();
        repository.save(ana);
        session.findCurrentUser();

        // Act
        ana.activate();
        repository.save(ana);

        // Assert
        assertThat(session.findCurrentUser()).isEmpty();
    }

    @Test
    void aNewRoleOrNameAppliesToTheNextOperation() {
        // Arrange
        User ana = repository.save(user("ana@example.com", UserRole.CUSTOMER));
        session.signIn(ana);

        // Act
        ana.changeRole(UserRole.MERCHANT);
        ana.updateProfile("Ana Maria", "Perez", null);
        repository.save(ana);

        // Assert
        CurrentUser current = session.currentUser();
        assertThat(current.role()).isEqualTo(org.jala.edu.core.contract.UserRole.MERCHANT);
        assertThat(current.displayName()).isEqualTo("Ana Maria Perez");
    }

    @Test
    void aUserThatNoLongerExistsIsSignedOut() {
        // Arrange
        User ana = repository.save(user("ana@example.com"));
        session.signIn(ana);

        // Act
        repository.deleteById(ana.getId());

        // Assert
        assertThat(session.findCurrentUser()).isEmpty();
    }

    @Test
    void theSessionEndsAfterTheIdleTimeoutAndStaysEnded() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-09T10:00:00Z"));
        SessionCurrentUserService expiring =
                new SessionCurrentUserService(new FindUserUseCase(repository), Duration.ofMinutes(30), clock);
        expiring.signIn(repository.save(user("ana@example.com")));

        // Act
        clock.advance(Duration.ofMinutes(31));

        // Assert
        assertThat(expiring.findCurrentUser()).isEmpty();
        clock.advance(Duration.ofSeconds(1));
        assertThat(expiring.findCurrentUser()).isEmpty();
    }

    @Test
    void eachLookupKeepsTheSessionAlive() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2026-11-09T10:00:00Z"));
        SessionCurrentUserService expiring =
                new SessionCurrentUserService(new FindUserUseCase(repository), Duration.ofMinutes(30), clock);
        expiring.signIn(repository.save(user("ana@example.com")));

        // Act
        clock.advance(Duration.ofMinutes(20));
        expiring.findCurrentUser();
        clock.advance(Duration.ofMinutes(20));

        // Assert
        assertThat(expiring.findCurrentUser()).isPresent();
    }

    /**
     * A clock the test moves forward by hand.
     */
    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
