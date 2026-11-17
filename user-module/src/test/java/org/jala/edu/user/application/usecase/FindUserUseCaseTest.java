package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.user.UserFixtures.user;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;

class FindUserUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final FindUserUseCase findUser = new FindUserUseCase(repository);

    @Test
    void returnsTheUserWithTheGivenId() {
        // Arrange
        User ana = repository.save(user("ana@mail.com"));

        // Act
        Optional<User> found = findUser.execute(ana.getId().value());

        // Assert
        assertThat(found).contains(ana);
    }

    @Test
    void returnsAnInactiveUserToo() {
        // Arrange
        User ana = user("ana@mail.com");
        ana.deactivate();
        repository.save(ana);

        // Act
        Optional<User> found = findUser.execute(ana.getId().value());

        // Assert
        assertThat(found).contains(ana);
    }

    @Test
    void returnsEmptyForAnUnknownId() {
        // Arrange
        repository.save(user("ana@mail.com"));

        // Act
        Optional<User> found = findUser.execute(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }
}
