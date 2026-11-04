package org.jala.edu.core.infrastructure.contract;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.contract.UserSummary;

/**
 * Stand-in {@link UserService} that only knows the {@link SampleUsers}, with the same names and emails
 * {@code user-module} stores for them. Teams can also use it in their unit tests.
 */
public class FallbackUserService implements UserService {

    private static final List<UserSummary> SAMPLE_USERS = List.of(
            sample(SampleUsers.ADMIN, "Admin", "admin@sample.delivery", UserRole.ADMIN, true),
            sample(SampleUsers.MERCHANT, "Merchant", "merchant@sample.delivery", UserRole.MERCHANT, true),
            sample(SampleUsers.CUSTOMER, "Customer", "customer@sample.delivery", UserRole.CUSTOMER, true),
            sample(SampleUsers.COURIER, "Courier", "courier@sample.delivery", UserRole.COURIER, true),
            sample(SampleUsers.INACTIVE_CUSTOMER, "Inactive", "inactive@sample.delivery", UserRole.CUSTOMER, false),
            sample(SampleUsers.INACTIVE_COURIER, "Inactive Courier", "inactive.courier@sample.delivery",
                    UserRole.COURIER, false));

    @Override
    public boolean isActiveWithRole(UUID userId, UserRole... roles) {
        return findUser(userId)
                .filter(UserSummary::active)
                .map(user -> Arrays.asList(roles).contains(user.role()))
                .orElse(false);
    }

    @Override
    public Optional<UserSummary> findUser(UUID userId) {
        return SAMPLE_USERS.stream().filter(user -> user.id().equals(userId)).findFirst();
    }

    @Override
    public List<UserSummary> findActiveByRole(UserRole role) {
        return SAMPLE_USERS.stream()
                .filter(user -> user.active() && user.role() == role)
                .sorted(Comparator.comparing(UserSummary::fullName))
                .toList();
    }

    private static UserSummary sample(UUID id, String lastName, String email, UserRole role, boolean active) {
        return new UserSummary(id, "Sample", lastName, email, null, role, active);
    }
}
