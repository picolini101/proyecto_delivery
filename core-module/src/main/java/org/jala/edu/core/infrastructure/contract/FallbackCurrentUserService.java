package org.jala.edu.core.infrastructure.contract;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;

/**
 * Stand-in {@link CurrentUserService} that always answers with one of the {@link SampleUsers}, chosen with the
 * {@value #PROPERTY} property, so each team can try its screens with the role it needs before the login exists.
 */
public class FallbackCurrentUserService implements CurrentUserService {

    public static final String PROPERTY = "delivery.sample-user";
    public static final UserRole DEFAULT_ROLE = UserRole.CUSTOMER;

    private static final Map<UserRole, CurrentUser> SAMPLE_USERS = Map.of(
            UserRole.ADMIN, new CurrentUser(SampleUsers.ADMIN, UserRole.ADMIN, "Sample Admin"),
            UserRole.MERCHANT, new CurrentUser(SampleUsers.MERCHANT, UserRole.MERCHANT, "Sample Merchant"),
            UserRole.CUSTOMER, new CurrentUser(SampleUsers.CUSTOMER, UserRole.CUSTOMER, "Sample Customer"),
            UserRole.COURIER, new CurrentUser(SampleUsers.COURIER, UserRole.COURIER, "Sample Courier"));

    private final CurrentUser sampleUser;

    public FallbackCurrentUserService(UserRole role) {
        this.sampleUser = SAMPLE_USERS.get(role);
    }

    /**
     * Reads the role from the property value; blank means {@link #DEFAULT_ROLE}.
     *
     * @throws IllegalArgumentException if the value is not a {@link UserRole}
     */
    public static FallbackCurrentUserService forProperty(String value) {
        if (value == null || value.isBlank()) {
            return new FallbackCurrentUserService(DEFAULT_ROLE);
        }
        try {
            return new FallbackCurrentUserService(UserRole.valueOf(value.strip().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + PROPERTY + " '" + value + "'; use ADMIN, MERCHANT, "
                    + "CUSTOMER or COURIER", e);
        }
    }

    @Override
    public Optional<CurrentUser> findCurrentUser() {
        return Optional.of(sampleUser);
    }
}
