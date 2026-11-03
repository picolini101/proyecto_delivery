package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.user.UserFixtures.user;
import static org.jala.edu.user.UserFixtures.userCreatedAt;

import java.util.List;
import org.jala.edu.user.application.port.in.ListUsersQuery;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListUsersUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final ListUsersUseCase listUsers = new ListUsersUseCase(repository);

    @Test
    void returnsAnEmptyListWhenThereAreNoUsers() {
        // Arrange
        ListUsersQuery query = ListUsersQuery.all();

        // Act
        List<User> users = listUsers.execute(query);

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void returnsAllUsersOldestFirst() {
        // Arrange
        User newer = repository.save(userCreatedAt("luis@example.com", "2026-02-01T10:00:00Z"));
        User older = repository.save(userCreatedAt("ana@example.com", "2026-01-01T10:00:00Z"));

        // Act
        List<User> users = listUsers.execute(ListUsersQuery.all());

        // Assert
        assertThat(users).containsExactly(older, newer);
    }

    @Test
    void filtersByPartOfTheEmailIgnoringCase() {
        // Arrange
        User ana = repository.save(user("ana@mail.com"));
        repository.save(user("luis@example.com"));

        // Act
        List<User> users = listUsers.execute(new ListUsersQuery("  ANA@Mail "));

        // Assert
        assertThat(users).containsExactly(ana);
    }

    @Test
    void returnsAnEmptyListWhenNoEmailMatches() {
        // Arrange
        repository.save(user("ana@mail.com"));

        // Act
        List<User> users = listUsers.execute(new ListUsersQuery("pedro"));

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    void aBlankFilterListsEveryUser() {
        // Arrange
        repository.save(user("ana@mail.com"));
        repository.save(user("luis@example.com"));

        // Act
        List<User> users = listUsers.execute(new ListUsersQuery("   "));

        // Assert
        assertThat(users).hasSize(2);
    }
}
