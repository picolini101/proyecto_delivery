package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.createCommand;
import static org.jala.edu.user.UserFixtures.user;

import java.util.UUID;
import org.jala.edu.user.application.port.in.CreateUserCommand;
import org.jala.edu.user.domain.exception.EmailAlreadyRegisteredException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.security.BCryptPasswordHasher;
import org.jala.edu.user.domain.model.Password;
import org.junit.jupiter.api.Test;

class CreateUserUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();
    private final CreateUserUseCase createUser = new CreateUserUseCase(repository, hasher);

    @Test
    void storesAnActiveUserAndReturnsItsId() {
        // Arrange
        CreateUserCommand command = new CreateUserCommand("Ana", "Perez", "Ana@Example.com", "+59170000000",
                UserRole.COURIER, "Secret123");

        // Act
        UUID id = createUser.execute(command);

        // Assert
        User stored = repository.findById(new UserId(id)).orElseThrow();
        assertThat(stored.fullName()).isEqualTo("Ana Perez");
        assertThat(stored.getEmail().value()).isEqualTo("ana@example.com");
        assertThat(stored.getPhoneNumber()).isEqualTo("+59170000000");
        assertThat(stored.getRole()).isEqualTo(UserRole.COURIER);
        assertThat(stored.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegisteredIgnoringCase() {
        // Arrange
        repository.save(user("ana@example.com"));
        CreateUserCommand duplicate = createCommand("ANA@example.com");

        // Act
        Throwable thrown = catchThrowable(() -> createUser.execute(duplicate));

        // Assert
        assertThat(thrown).isInstanceOf(EmailAlreadyRegisteredException.class);
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void storesOnlyTheBCryptHashOfThePassword() {
        // Arrange
        CreateUserCommand command = org.jala.edu.user.UserFixtures.createCommand("luis@example.com");

        // Act
        UUID id = createUser.execute(command);

        // Assert
        User stored = repository.findById(new org.jala.edu.user.domain.model.UserId(id)).orElseThrow();
        assertThat(stored.getPasswordHash()).isNotEqualTo(command.password()).startsWith("$2");
        assertThat(hasher.matches(new Password(command.password()), stored.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsAPasswordShorterThanEightCharacters() {
        // Arrange
        CreateUserCommand command = new CreateUserCommand("Ana", "Perez", "ana@example.com", null,
                UserRole.CUSTOMER, "short");

        // Act
        Throwable thrown = catchThrowable(() -> createUser.execute(command));

        // Assert
        assertThat(thrown).hasMessage("Password must have at least 8 characters");
        assertThat(repository.findAll()).isEmpty();
    }
}
