package org.jala.edu.user.infrastructure.config;

import java.time.Instant;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Stores the {@link SampleUsers} at startup with their fixed ids, so other teams can test against the real
 * {@code UserService}. Users that already exist are left untouched, except that a sample user without a password
 * (stored before D-10) receives {@link #SAMPLE_PASSWORD}.
 */
public class SampleUsersLoader implements ApplicationRunner {

    /**
     * Shared test password of every sample user, documented in the README. Never use it for a real account.
     */
    public static final String SAMPLE_PASSWORD = "Sample123";

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;
    private String sampleHash;

    public SampleUsersLoader(UserRepositoryPort userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        load();
    }

    void load() {
        store(SampleUsers.ADMIN, "Admin", "admin@sample.delivery", UserRole.ADMIN, UserStatus.ACTIVE);
        store(SampleUsers.MERCHANT, "Merchant", "merchant@sample.delivery", UserRole.MERCHANT, UserStatus.ACTIVE);
        store(SampleUsers.CUSTOMER, "Customer", "customer@sample.delivery", UserRole.CUSTOMER, UserStatus.ACTIVE);
        store(SampleUsers.COURIER, "Courier", "courier@sample.delivery", UserRole.COURIER, UserStatus.ACTIVE);
        store(SampleUsers.INACTIVE_CUSTOMER, "Inactive", "inactive@sample.delivery", UserRole.CUSTOMER,
                UserStatus.INACTIVE);
        store(SampleUsers.INACTIVE_COURIER, "Inactive Courier", "inactive.courier@sample.delivery", UserRole.COURIER,
                UserStatus.INACTIVE);
    }

    private void store(UUID id, String lastName, String email, UserRole role, UserStatus status) {
        UserId userId = new UserId(id);
        Email userEmail = new Email(email);
        User existing = userRepository.findById(userId).orElse(null);
        if (existing != null) {
            if (!existing.hasPassword()) {
                existing.changePasswordHash(sampleHash());
                userRepository.save(existing);
            }
            return;
        }
        if (userRepository.existsByEmail(userEmail)) {
            return;
        }
        Instant now = Instant.now();
        userRepository.save(User.restore(userId, "Sample", lastName, userEmail, null, role, status, sampleHash(),
                now, now));
    }

    /**
     * Hashes the sample password once per start: BCrypt is slow on purpose.
     */
    private String sampleHash() {
        if (sampleHash == null) {
            sampleHash = passwordHasher.hash(new Password(SAMPLE_PASSWORD));
        }
        return sampleHash;
    }
}
