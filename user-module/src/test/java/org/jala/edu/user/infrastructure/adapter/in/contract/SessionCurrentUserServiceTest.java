package org.jala.edu.user.infrastructure.adapter.in.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jala.edu.user.UserFixtures.user;

import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.NotAuthenticatedException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.junit.jupiter.api.Test;

class SessionCurrentUserServiceTest {

    private final SessionCurrentUserService session = new SessionCurrentUserService();

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
        User courier = user("ana@example.com", UserRole.COURIER);

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
        session.signIn(user("ana@example.com"));

        // Act
        session.signOut();

        // Assert
        assertThat(session.findCurrentUser()).isEmpty();
    }
}
