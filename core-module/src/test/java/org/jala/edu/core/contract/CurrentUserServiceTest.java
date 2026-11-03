package org.jala.edu.core.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class CurrentUserServiceTest {

    @Test
    void currentUserReturnsTheSignedInUser() {
        // Arrange
        CurrentUser customer = new CurrentUser(SampleUsers.CUSTOMER, UserRole.CUSTOMER, "Sample Customer");
        CurrentUserService service = () -> Optional.of(customer);

        // Act
        CurrentUser current = service.currentUser();

        // Assert
        assertThat(current).isEqualTo(customer);
    }

    @Test
    void currentUserFailsWhenNobodyHasSignedIn() {
        // Arrange
        CurrentUserService service = Optional::empty;

        // Act + Assert
        assertThat(service.findCurrentUser()).isEmpty();
        assertThatThrownBy(service::currentUser)
                .isInstanceOf(NotAuthenticatedException.class)
                .hasMessage("No user has signed in");
    }
}
