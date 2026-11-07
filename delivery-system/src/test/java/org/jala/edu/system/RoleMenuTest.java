package org.jala.edu.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.ui.NavigationItem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoleMenuTest {

    private static final List<String> TITLES = List.of("Users", "Catalog", "Browse catalog", "Orders",
            "My commerce orders", "Payments", "Deliveries");

    private final RoleMenu menu = new RoleMenu();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "ADMIN    | Users,Catalog",
            "MERCHANT | Catalog,My commerce orders",
            "CUSTOMER | Browse catalog,Orders,Payments",
            "COURIER  | Deliveries"})
    void eachRoleSeesOnlyItsModules(UserRole role, String expected) {
        // Arrange
        CurrentUser user = new CurrentUser(UUID.randomUUID(), role, "Sample");

        // Act
        List<String> visible = TITLES.stream()
                .filter(title -> menu.isVisible(new NavigationItem(title, "/view.fxml"), user))
                .toList();

        // Assert
        assertThat(visible).containsExactly(expected.split(","));
    }

    @ParameterizedTest
    @CsvSource({"ADMIN", "COURIER"})
    void aModuleWithoutRolesIsShownToEveryone(UserRole role) {
        // Arrange
        CurrentUser user = new CurrentUser(UUID.randomUUID(), role, "Sample");

        // Act
        boolean visible = menu.isVisible(new NavigationItem("Reports", "/reports.fxml"), user);

        // Assert
        assertThat(visible).isTrue();
    }
}
