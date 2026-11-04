package org.jala.edu.user.infrastructure.adapter.in.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.user.UserFixtures.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserSummary;
import org.jala.edu.user.UserFixtures;
import org.jala.edu.user.application.usecase.CheckUserAccessUseCase;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.application.usecase.ListActiveUsersByRoleUseCase;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.infrastructure.adapter.out.persistence.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UserServiceAdapterTest {

    private final InMemoryUserRepositoryAdapter repository = new InMemoryUserRepositoryAdapter();
    private final UserServiceAdapter userService =
            new UserServiceAdapter(new CheckUserAccessUseCase(repository), new FindUserUseCase(repository),
                    new ListActiveUsersByRoleUseCase(repository));

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void everyContractRoleMatchesTheUserRoleWithTheSameName(UserRole contractRole) {
        // Arrange
        User user = user("ana@example.com");
        user.changeRole(org.jala.edu.user.domain.model.UserRole.valueOf(contractRole.name()));
        repository.save(user);

        // Act
        boolean allowed = userService.isActiveWithRole(user.getId().value(), contractRole);

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void rejectsWhenNoRoleIsRequested() {
        // Arrange
        UUID customerId = repository.save(user("ana@example.com")).getId().value();

        // Act
        boolean allowed = userService.isActiveWithRole(customerId);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void findUserReturnsThePublicDataOfTheUser() {
        // Arrange
        User ana = repository.save(user("ana@example.com", org.jala.edu.user.domain.model.UserRole.MERCHANT));

        // Act
        Optional<UserSummary> found = userService.findUser(ana.getId().value());

        // Assert
        assertThat(found).hasValueSatisfying(summary -> {
            assertThat(summary.id()).isEqualTo(ana.getId().value());
            assertThat(summary.fullName()).isEqualTo("Ana Perez");
            assertThat(summary.email()).isEqualTo("ana@example.com");
            assertThat(summary.phoneNumber()).isEqualTo(UserFixtures.PHONE);
            assertThat(summary.role()).isEqualTo(UserRole.MERCHANT);
            assertThat(summary.active()).isTrue();
        });
    }

    @Test
    void findUserIsEmptyForAnUnknownId() {
        // Arrange
        UUID unknown = UUID.randomUUID();

        // Act
        Optional<UserSummary> found = userService.findUser(unknown);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findActiveByRoleListsOnlyActiveUsersOfThatRoleByName() {
        // Arrange
        User zoe = repository.save(User.register("Zoe", "Diaz", new Email("zoe@example.com"), null,
                org.jala.edu.user.domain.model.UserRole.MERCHANT));
        User bruno = repository.save(User.register("Bruno", "Rios", new Email("bruno@example.com"), null,
                org.jala.edu.user.domain.model.UserRole.MERCHANT));
        User inactive = user("off@example.com", org.jala.edu.user.domain.model.UserRole.MERCHANT);
        inactive.deactivate();
        repository.save(inactive);
        repository.save(user("customer@example.com"));

        // Act
        List<UserSummary> merchants = userService.findActiveByRole(UserRole.MERCHANT);

        // Assert
        assertThat(merchants).extracting(UserSummary::id)
                .containsExactly(bruno.getId().value(), zoe.getId().value());
    }
}
