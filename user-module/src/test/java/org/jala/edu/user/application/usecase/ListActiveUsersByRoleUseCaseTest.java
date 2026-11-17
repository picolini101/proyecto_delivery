package org.jala.edu.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.user.UserFixtures.PHONE;
import static org.jala.edu.user.UserFixtures.user;

import java.util.List;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListActiveUsersByRoleUseCaseTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final ListActiveUsersByRoleUseCase listActiveByRole = new ListActiveUsersByRoleUseCase(repository);

    @Test
    void returnsOnlyTheActiveUsersOfTheRole() {
        // Arrange
        User merchant = repository.save(user("shop@mail.com", UserRole.MERCHANT));
        User inactiveMerchant = user("closed@mail.com", UserRole.MERCHANT);
        inactiveMerchant.deactivate();
        repository.save(inactiveMerchant);
        repository.save(user("courier@mail.com", UserRole.COURIER));

        // Act
        List<User> merchants = listActiveByRole.execute(UserRole.MERCHANT);

        // Assert
        assertThat(merchants).containsExactly(merchant);
    }

    @Test
    void sortsTheUsersByFullName() {
        // Arrange
        User zoe = repository.save(named("zoe@mail.com", "Zoe", "Rios"));
        User bruno = repository.save(named("bruno@mail.com", "Bruno", "Diaz"));
        User ana = repository.save(named("ana@mail.com", "Ana", "Mendez"));

        // Act
        List<User> merchants = listActiveByRole.execute(UserRole.MERCHANT);

        // Assert
        assertThat(merchants).containsExactly(ana, bruno, zoe);
    }

    @Test
    void returnsAnEmptyListWhenNoActiveUserHasTheRole() {
        // Arrange
        repository.save(user("courier@mail.com", UserRole.COURIER));

        // Act
        List<User> merchants = listActiveByRole.execute(UserRole.MERCHANT);

        // Assert
        assertThat(merchants).isEmpty();
    }

    private static User named(String email, String firstName, String lastName) {
        User merchant = user(email, UserRole.MERCHANT);
        merchant.updateProfile(firstName, lastName, PHONE);
        return merchant;
    }
}
