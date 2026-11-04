package org.jala.edu.core.infrastructure.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserSummary;
import org.junit.jupiter.api.Test;

class FallbackUserServiceTest {

    private final FallbackUserService userService = new FallbackUserService();

    @Test
    void acceptsASampleUserWithOneOfTheRequestedRoles() {
        // Arrange
        UUID merchant = SampleUsers.MERCHANT;

        // Act
        boolean allowed = userService.isActiveWithRole(merchant, UserRole.ADMIN, UserRole.MERCHANT);

        // Assert
        assertThat(allowed).isTrue();
    }

    @Test
    void rejectsASampleUserWithAnotherRole() {
        // Arrange
        UUID courier = SampleUsers.COURIER;

        // Act
        boolean allowed = userService.isActiveWithRole(courier, UserRole.CUSTOMER);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void rejectsTheInactiveSampleUserAndUnknownIds() {
        // Arrange
        UUID inactive = SampleUsers.INACTIVE_CUSTOMER;
        UUID unknown = UUID.randomUUID();

        // Act
        boolean inactiveAllowed = userService.isActiveWithRole(inactive, UserRole.CUSTOMER);
        boolean unknownAllowed = userService.isActiveWithRole(unknown, UserRole.CUSTOMER);

        // Assert
        assertThat(inactiveAllowed).isFalse();
        assertThat(unknownAllowed).isFalse();
    }

    @Test
    void rejectsTheInactiveSampleCourier() {
        // Arrange
        UUID inactiveCourier = SampleUsers.INACTIVE_COURIER;

        // Act
        boolean allowed = userService.isActiveWithRole(inactiveCourier, UserRole.COURIER);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void findsASampleUserWithTheDataUserModuleStores() {
        // Arrange
        UUID merchant = SampleUsers.MERCHANT;

        // Act
        Optional<UserSummary> found = userService.findUser(merchant);

        // Assert
        assertThat(found).hasValueSatisfying(user -> {
            assertThat(user.fullName()).isEqualTo("Sample Merchant");
            assertThat(user.email()).isEqualTo("merchant@sample.delivery");
            assertThat(user.role()).isEqualTo(UserRole.MERCHANT);
            assertThat(user.active()).isTrue();
        });
    }

    @Test
    void findsInactiveSampleUsersButNotUnknownIds() {
        // Arrange
        UUID inactive = SampleUsers.INACTIVE_CUSTOMER;

        // Act
        Optional<UserSummary> found = userService.findUser(inactive);
        Optional<UserSummary> unknown = userService.findUser(UUID.randomUUID());

        // Assert
        assertThat(found).hasValueSatisfying(user -> assertThat(user.active()).isFalse());
        assertThat(unknown).isEmpty();
    }

    @Test
    void listsOnlyTheActiveSampleUsersOfARole() {
        // Arrange
        UserRole role = UserRole.COURIER;

        // Act
        List<UserSummary> couriers = userService.findActiveByRole(role);

        // Assert
        assertThat(couriers).extracting(UserSummary::id).containsExactly(SampleUsers.COURIER);
    }
}
