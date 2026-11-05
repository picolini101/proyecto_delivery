package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_ADMIN;
import static org.jala.edu.user.UserFixtures.SIGNED_IN_CUSTOMER;
import static org.jala.edu.user.UserFixtures.signedInAs;
import static org.jala.edu.user.UserFixtures.user;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.user.application.port.in.ChangeUserStatusCommand;
import org.jala.edu.user.domain.exception.AdminRequiredException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserStatusHistoryAdapter;
import org.junit.jupiter.api.Test;

class ListUserStatusHistoryUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final InMemoryUserStatusHistoryAdapter history = new InMemoryUserStatusHistoryAdapter();
    private final ChangeUserStatusUseCase changeStatus =
            new ChangeUserStatusUseCase(repository, history, signedInAs(SIGNED_IN_ADMIN),
                    TransactionRunner.direct());
    private final ListUserStatusHistoryUseCase listHistory =
            new ListUserStatusHistoryUseCase(repository, history, signedInAs(SIGNED_IN_ADMIN));

    @Test
    void listsTheChangesOfTheUserNewestFirst() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));
        UUID id = customer.getId().value();
        changeStatus.execute(new ChangeUserStatusCommand(id, UserStatus.INACTIVE));
        changeStatus.execute(new ChangeUserStatusCommand(id, UserStatus.ACTIVE));

        // Act
        List<UserStatusChange> changes = listHistory.execute(id);

        // Assert
        assertThat(changes).extracting(UserStatusChange::getNewStatus)
                .containsExactly(UserStatus.ACTIVE, UserStatus.INACTIVE);
    }

    @Test
    void returnsAnEmptyListForAUserWithoutChanges() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));

        // Act
        List<UserStatusChange> changes = listHistory.execute(customer.getId().value());

        // Assert
        assertThat(changes).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnAdministrator() {
        // Arrange
        User customer = repository.save(user("ana@example.com"));
        ListUserStatusHistoryUseCase asCustomer =
                new ListUserStatusHistoryUseCase(repository, history, signedInAs(SIGNED_IN_CUSTOMER));

        // Act
        Throwable error = catchThrowable(() -> asCustomer.execute(customer.getId().value()));

        // Assert
        assertThat(error).isInstanceOf(AdminRequiredException.class);
    }

    @Test
    void rejectsAnUnknownUser() {
        // Act
        Throwable error = catchThrowable(() -> listHistory.execute(UUID.randomUUID()));

        // Assert
        assertThat(error).isInstanceOf(UserNotFoundException.class);
    }
}
