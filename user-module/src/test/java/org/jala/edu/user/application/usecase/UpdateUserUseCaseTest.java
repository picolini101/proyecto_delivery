package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_ADMIN;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_CUSTOMER;
import static org.jala.edu.user.UserFixtures.signedInAs;
import static org.jala.edu.user.UserFixtures.user;

import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.UpdateUserCommand;
import org.jala.edu.user.domain.exception.AdminRequiredException;
import org.jala.edu.user.domain.exception.LastActiveAdminException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserStatusHistoryAdapter;
import org.junit.jupiter.api.Test;

class UpdateUserUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final InMemoryUserStatusHistoryAdapter history = new InMemoryUserStatusHistoryAdapter();
    private final UpdateUserUseCase updateUser =
            new UpdateUserUseCase(repository, history, signedInAs(SIGNED_IN_ADMIN), TransactionRunner.direct());

    @Test
    void updatesProfileRoleAndStatus() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserCommand command = new UpdateUserCommand(existing.getId().value(), "Ana Maria", "Lopez", "+59171111111",
                UserRole.COURIER, UserStatus.INACTIVE);

        // Act
        updateUser.execute(command);

        // Assert
        User stored = repository.findById(existing.getId()).orElseThrow();
        assertThat(stored.fullName()).isEqualTo("Ana Maria Lopez");
        assertThat(stored.getPhoneNumber()).isEqualTo("+59171111111");
        assertThat(stored.getRole()).isEqualTo(UserRole.COURIER);
        assertThat(stored.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void keepsTheEmail() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserCommand command = new UpdateUserCommand(existing.getId().value(), "Ana Maria", "Lopez", null,
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Act
        updateUser.execute(command);

        // Assert
        User stored = repository.findById(existing.getId()).orElseThrow();
        assertThat(stored.getEmail().value()).isEqualTo("ana@example.com");
    }

    @Test
    void rejectsBlankNamesAndLeavesTheUserUnchanged() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserCommand invalid = new UpdateUserCommand(existing.getId().value(), "Maria", " ", null,
                UserRole.ADMIN, UserStatus.ACTIVE);

        // Act
        Throwable thrown = catchThrowable(() -> updateUser.execute(invalid));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessageContaining("Last name");
        User stored = repository.findById(existing.getId()).orElseThrow();
        assertThat(stored.fullName()).isEqualTo("Ana Perez");
        assertThat(stored.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void rejectsDemotingTheOnlyActiveAdminAndLeavesItUnchanged() {
        // Arrange
        User admin = repository.save(user("admin@example.com", UserRole.ADMIN));
        UpdateUserCommand demote = new UpdateUserCommand(admin.getId().value(), "Ana", "Perez", null,
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Act
        Throwable thrown = catchThrowable(() -> updateUser.execute(demote));

        // Assert
        assertThat(thrown).isInstanceOf(LastActiveAdminException.class)
                .hasMessage("There must be at least one active administrator");
        assertThat(repository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void demotesAnAdminWhenAnotherAdminIsActive() {
        // Arrange
        User admin = repository.save(user("admin@example.com", UserRole.ADMIN));
        repository.save(user("second-admin@example.com", UserRole.ADMIN));
        UpdateUserCommand demote = new UpdateUserCommand(admin.getId().value(), "Ana", "Perez", null,
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Act
        updateUser.execute(demote);

        // Assert
        assertThat(repository.findById(admin.getId()).orElseThrow().getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void throwsWhenTheUserDoesNotExist() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand(UUID.randomUUID(), "Ana", "Perez", null,
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Act
        Throwable thrown = catchThrowable(() -> updateUser.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void recordsTheStatusChangeMadeFromTheEditForm() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserCommand command = new UpdateUserCommand(existing.getId().value(), "Ana", "Perez", null,
                UserRole.CUSTOMER, UserStatus.INACTIVE);

        // Act
        updateUser.execute(command);

        // Assert
        assertThat(history.findByUserIdNewestFirst(existing.getId()))
                .singleElement()
                .satisfies(change -> assertThat(change.getNewStatus()).isEqualTo(UserStatus.INACTIVE));
    }

    @Test
    void recordsNothingWhenOnlyTheProfileChanges() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserCommand command = new UpdateUserCommand(existing.getId().value(), "Ana", "Perez", null,
                UserRole.CUSTOMER, UserStatus.ACTIVE);

        // Act
        updateUser.execute(command);

        // Assert
        assertThat(history.findByUserIdNewestFirst(existing.getId())).isEmpty();
    }

    @Test
    void onlyAnAdministratorCanEditUsers() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        UpdateUserUseCase asCustomer = new UpdateUserUseCase(repository, history, signedInAs(SIGNED_IN_CUSTOMER),
                TransactionRunner.direct());
        UpdateUserCommand promote = new UpdateUserCommand(existing.getId().value(), "Ana", "Perez", null,
                UserRole.ADMIN, UserStatus.ACTIVE);

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(promote));

        // Assert
        assertThat(thrown).isInstanceOf(AdminRequiredException.class);
        assertThat(repository.findById(existing.getId()).orElseThrow().getRole()).isEqualTo(UserRole.CUSTOMER);
    }
}
