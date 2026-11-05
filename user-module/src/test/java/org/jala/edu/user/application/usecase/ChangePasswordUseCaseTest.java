package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.PASSWORD;
import static org.jala.edu.user.UserFixtures.user;

import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.ChangePasswordCommand;
import org.jala.edu.user.domain.exception.WrongCurrentPasswordException;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangePasswordUseCaseTest {

    private static final String NEW_PASSWORD = "Better456";

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final ChangePasswordUseCase changePassword = new ChangePasswordUseCase(repository, hasher);

    private User ana;

    @BeforeEach
    void storeAUserWithAPassword() {
        ana = user("ana@example.com");
        ana.changePasswordHash(hasher.hash(new Password(PASSWORD)));
        repository.save(ana);
    }

    @Test
    void storesTheHashOfTheNewPassword() {
        // Arrange
        ChangePasswordCommand command = new ChangePasswordCommand(ana.getId().value(), PASSWORD, NEW_PASSWORD);

        // Act
        changePassword.execute(command);

        // Assert
        String hash = repository.findById(ana.getId()).orElseThrow().getPasswordHash();
        assertThat(hasher.matches(new Password(NEW_PASSWORD), hash)).isTrue();
        assertThat(hasher.matches(new Password(PASSWORD), hash)).isFalse();
    }

    @Test
    void rejectsAWrongCurrentPasswordAndKeepsTheOldOne() {
        // Arrange
        String before = ana.getPasswordHash();
        ChangePasswordCommand command = new ChangePasswordCommand(ana.getId().value(), "Wrong1234", NEW_PASSWORD);

        // Act
        Throwable thrown = catchThrowable(() -> changePassword.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(WrongCurrentPasswordException.class)
                .hasMessage("Current password is incorrect");
        assertThat(repository.findById(ana.getId()).orElseThrow().getPasswordHash()).isEqualTo(before);
    }

    @Test
    void treatsATooShortCurrentPasswordAsWrong() {
        // Arrange
        ChangePasswordCommand command = new ChangePasswordCommand(ana.getId().value(), "short", NEW_PASSWORD);

        // Act
        Throwable thrown = catchThrowable(() -> changePassword.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(WrongCurrentPasswordException.class);
    }

    @Test
    void rejectsANewPasswordShorterThanTheMinimum() {
        // Arrange
        String before = ana.getPasswordHash();
        ChangePasswordCommand command = new ChangePasswordCommand(ana.getId().value(), PASSWORD, "short");

        // Act
        Throwable thrown = catchThrowable(() -> changePassword.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Password must have at least " + Password.MIN_LENGTH + " characters");
        assertThat(repository.findById(ana.getId()).orElseThrow().getPasswordHash()).isEqualTo(before);
    }
}
