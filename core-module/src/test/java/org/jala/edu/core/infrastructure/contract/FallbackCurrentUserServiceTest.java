package org.jala.edu.core.infrastructure.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.junit.jupiter.api.Test;

class FallbackCurrentUserServiceTest {

    @Test
    void usesTheSampleCustomerWhenThePropertyIsMissing() {
        // Arrange
        FallbackCurrentUserService service = FallbackCurrentUserService.forProperty(null);

        // Act
        CurrentUser current = service.currentUser();

        // Assert
        assertThat(current.id()).isEqualTo(SampleUsers.CUSTOMER);
        assertThat(current.role()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void usesTheSampleUserOfTheConfiguredRoleIgnoringCase() {
        // Arrange
        FallbackCurrentUserService service = FallbackCurrentUserService.forProperty(" courier ");

        // Act
        CurrentUser current = service.currentUser();

        // Assert
        assertThat(current).isEqualTo(new CurrentUser(SampleUsers.COURIER, UserRole.COURIER, "Sample Courier"));
    }

    @Test
    void rejectsAnUnknownRole() {
        // Arrange
        String value = "DRIVER";

        // Act + Assert
        assertThatThrownBy(() -> FallbackCurrentUserService.forProperty(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("delivery.sample-user");
    }
}
