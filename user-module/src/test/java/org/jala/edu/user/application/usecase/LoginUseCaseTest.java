package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.user;

import org.jala.edu.user.application.port.in.LoginCommand;
import org.jala.edu.user.domain.exception.InactiveUserException;
import org.jala.edu.user.domain.exception.InvalidCredentialsException;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LoginUseCaseTest {

    private static final String PASSWORD = "Secret123";

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final LoginUseCase login = new LoginUseCase(repository, hasher);

    @Test
    void returnsTheActiveUserWithTheRightPassword() {
        // Arrange
        User ana = repository.save(userWithPassword("ana@example.com"));

        // Act
        User signedIn = login.execute(new LoginCommand(" ANA@example.com ", PASSWORD));

        // Assert
        assertThat(signedIn.getId()).isEqualTo(ana.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "ana@example.com, Wrong1234",
            "luis@example.com, Secret123",
            "not-an-email, Secret123",
            "ana@example.com, short"})
    void rejectsWrongCredentialsWithoutSayingWhichOneFailed(String email, String password) {
        // Arrange
        repository.save(userWithPassword("ana@example.com"));

        // Act
        Throwable thrown = catchThrowable(() -> login.execute(new LoginCommand(email, password)));

        // Assert
        assertThat(thrown).isInstanceOf(InvalidCredentialsException.class).hasMessage("Invalid credentials");
    }

    @Test
    void rejectsAnInactiveUser() {
        // Arrange
        User ana = userWithPassword("ana@example.com");
        ana.deactivate();
        repository.save(ana);

        // Act
        Throwable thrown = catchThrowable(() -> login.execute(new LoginCommand("ana@example.com", PASSWORD)));

        // Assert
        assertThat(thrown).isInstanceOf(InactiveUserException.class).hasMessage("Inactive user");
    }

    @Test
    void rejectsAUserWithoutPassword() {
        // Arrange
        repository.save(user("ana@example.com"));

        // Act
        Throwable thrown = catchThrowable(() -> login.execute(new LoginCommand("ana@example.com", PASSWORD)));

        // Assert
        assertThat(thrown).isInstanceOf(InvalidCredentialsException.class);
    }

    private User userWithPassword(String email) {
        User user = user(email);
        user.changePasswordHash(hasher.hash(new Password(PASSWORD)));
        return user;
    }
}
