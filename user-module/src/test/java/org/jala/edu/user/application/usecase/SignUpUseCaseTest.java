package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.user;

import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.SignUpCommand;
import org.jala.edu.user.domain.exception.EmailAlreadyRegisteredException;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

class SignUpUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final SignUpUseCase signUp = new SignUpUseCase(new CreateUserUseCase(repository, hasher), repository);

    @Test
    void createsAnActiveCustomerThatCanSignIn() {
        // Arrange
        SignUpCommand command = new SignUpCommand("Ana", "Perez", "ana@example.com", null, "Secret123");

        // Act
        User created = signUp.execute(command);

        // Assert
        User stored = repository.findById(created.getId()).orElseThrow();
        assertThat(stored.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(stored.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(stored.getPhoneNumber()).isNull();
        assertThat(hasher.matches(new Password("Secret123"), stored.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsAnEmailThatAlreadyHasAnAccount() {
        // Arrange
        repository.save(user("ana@example.com", UserRole.COURIER));
        SignUpCommand duplicate = new SignUpCommand("Ana", "Perez", "ANA@example.com", null, "Secret123");

        // Act
        Throwable thrown = catchThrowable(() -> signUp.execute(duplicate));

        // Assert
        assertThat(thrown).isInstanceOf(EmailAlreadyRegisteredException.class);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void rejectsAShortPasswordWithoutCreatingTheAccount() {
        // Arrange
        SignUpCommand command = new SignUpCommand("Ana", "Perez", "ana@example.com", null, "short");

        // Act
        Throwable thrown = catchThrowable(() -> signUp.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(repository.findAll()).isEmpty();
    }
}
