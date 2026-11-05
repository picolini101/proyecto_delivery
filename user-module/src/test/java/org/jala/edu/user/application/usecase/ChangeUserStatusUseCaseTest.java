package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_ADMIN;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_CUSTOMER;
import static org.jala.edu.user.UserFixtures.signedInAs;
import static org.jala.edu.user.UserFixtures.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.user.application.port.in.ChangeUserStatusCommand;
import org.jala.edu.user.application.port.in.CheckUserAccessQuery;
import org.jala.edu.user.domain.exception.AdminRequiredException;
import org.jala.edu.user.domain.exception.LastActiveAdminException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserStatusHistoryAdapter;
import org.junit.jupiter.api.Test;

class ChangeUserStatusUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final InMemoryUserStatusHistoryAdapter history = new InMemoryUserStatusHistoryAdapter();
    private final ChangeUserStatusUseCase changeStatus =
            new ChangeUserStatusUseCase(repository, history, signedInAs(SIGNED_IN_ADMIN),
                    TransactionRunner.direct());
    private final CheckUserAccessUseCase checkAccess = new CheckUserAccessUseCase(repository);

    @Test
    void deactivatedUserLosesAccess() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));

        // Act
        changeStatus.execute(new ChangeUserStatusCommand(customer.getId().value(), UserStatus.INACTIVE));

        // Assert
        assertThat(repository.findById(customer.getId()).orElseThrow().getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(checkAccess.execute(new CheckUserAccessQuery(customer.getId().value(), Set.of(UserRole.CUSTOMER))))
                .isFalse();
    }

    @Test
    void activatesAnInactiveUser() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));
        customer.deactivate();
        repository.save(customer);

        // Act
        changeStatus.execute(new ChangeUserStatusCommand(customer.getId().value(), UserStatus.ACTIVE));

        // Assert
        assertThat(repository.findById(customer.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void rejectsDeactivatingTheOnlyActiveAdmin() {
        // Arrange
        User admin = repository.save(user("admin@example.com", UserRole.ADMIN));
        User inactiveAdmin = repository.save(user("old-admin@example.com", UserRole.ADMIN));
        inactiveAdmin.deactivate();
        repository.save(inactiveAdmin);

        // Act
        Throwable thrown = catchThrowable(() ->
                changeStatus.execute(new ChangeUserStatusCommand(admin.getId().value(), UserStatus.INACTIVE)));

        // Assert
        assertThat(thrown).isInstanceOf(LastActiveAdminException.class)
                .hasMessage("There must be at least one active administrator");
        assertThat(repository.findById(admin.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void deactivatesAnAdminWhenAnotherAdminIsActive() {
        // Arrange
        User admin = repository.save(user("admin@example.com", UserRole.ADMIN));
        repository.save(user("second-admin@example.com", UserRole.ADMIN));

        // Act
        changeStatus.execute(new ChangeUserStatusCommand(admin.getId().value(), UserStatus.INACTIVE));

        // Assert
        assertThat(repository.findById(admin.getId()).orElseThrow().isActive()).isFalse();
        assertThat(repository.countActiveByRole(UserRole.ADMIN)).isEqualTo(1);
    }

    @Test
    void throwsWhenTheUserDoesNotExist() {
        // Arrange
        ChangeUserStatusCommand command = new ChangeUserStatusCommand(UUID.randomUUID(), UserStatus.INACTIVE);

        // Act
        Throwable thrown = catchThrowable(() -> changeStatus.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void recordsWhoChangedTheStatus() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));

        // Act
        changeStatus.execute(new ChangeUserStatusCommand(customer.getId().value(), UserStatus.INACTIVE));

        // Assert
        assertThat(history.findByUserIdNewestFirst(customer.getId()))
                .singleElement()
                .satisfies(change -> {
                    assertThat(change.getPreviousStatus()).isEqualTo(UserStatus.ACTIVE);
                    assertThat(change.getNewStatus()).isEqualTo(UserStatus.INACTIVE);
                    assertThat(change.getChangedById()).isEqualTo(SIGNED_IN_ADMIN.id());
                    assertThat(change.getChangedByName()).isEqualTo("Sample Admin");
                });
    }

    @Test
    void recordsNothingWhenTheStatusDoesNotChange() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));

        // Act
        changeStatus.execute(new ChangeUserStatusCommand(customer.getId().value(), UserStatus.ACTIVE));

        // Assert
        assertThat(history.findByUserIdNewestFirst(customer.getId())).isEmpty();
    }

    @Test
    void onlyAnAdministratorCanChangeAStatus() {
        // Arrange
        User existing = repository.save(user("ana@example.com"));
        ChangeUserStatusUseCase asCustomer =
                new ChangeUserStatusUseCase(repository, history, signedInAs(SIGNED_IN_CUSTOMER),
                        TransactionRunner.direct());

        // Act
        Throwable thrown = catchThrowable(() ->
                asCustomer.execute(new ChangeUserStatusCommand(existing.getId().value(), UserStatus.INACTIVE)));

        // Assert
        assertThat(thrown).isInstanceOf(AdminRequiredException.class);
        assertThat(repository.findById(existing.getId()).orElseThrow().isActive()).isTrue();
        assertThat(history.findAll()).isEmpty();
    }

    @Test
    void storesTheNewStatusAndItsHistoryInOneTransaction() {
        // Arrange
        List<String> events = new ArrayList<>();
        InMemoryUserRepositoryAdapter users = new InMemoryUserRepositoryAdapter() {
            @Override
            public User save(User user) {
                events.add("save user");
                return super.save(user);
            }
        };
        InMemoryUserStatusHistoryAdapter changes = new InMemoryUserStatusHistoryAdapter() {
            @Override
            public UserStatusChange save(UserStatusChange change) {
                events.add("save history");
                return super.save(change);
            }
        };
        TransactionRunner transactions = new TransactionRunner() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                events.add("begin");
                T result = work.get();
                events.add("commit");
                return result;
            }
        };
        User customer = users.save(user("ana@example.com"));
        events.clear();
        ChangeUserStatusUseCase useCase =
                new ChangeUserStatusUseCase(users, changes, signedInAs(SIGNED_IN_ADMIN), transactions);

        // Act
        useCase.execute(new ChangeUserStatusCommand(customer.getId().value(), UserStatus.INACTIVE));

        // Assert
        assertThat(events).containsExactly("begin", "save user", "save history", "commit");
    }
}
