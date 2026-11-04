package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.user.UserFixtures.user;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.domain.model.UserStatusChangeId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/**
 * Runs the status history adapter against H2 in MySQL mode, with the schema created by the module's Flyway migrations.
 */
@DataJpaTest(properties = "spring.datasource.url=jdbc:h2:mem:status-history;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserPersistenceAdapter.class, UserStatusHistoryPersistenceAdapter.class})
class UserStatusHistoryPersistenceAdapterTest {

    private static final UUID ADMIN_ID = UUID.randomUUID();

    @Autowired
    private UserPersistenceAdapter users;

    @Autowired
    private UserStatusHistoryPersistenceAdapter history;

    @Autowired
    private EntityManager entityManager;

    @Test
    void readsBackEveryFieldOfAChange() {
        // Arrange
        User ana = users.save(user("ana@example.com"));
        UserStatusChange saved = history.save(UserStatusChange.record(ana.getId(), UserStatus.ACTIVE,
                UserStatus.INACTIVE, ADMIN_ID, "Sample Admin"));
        detach();

        // Act
        UserStatusChange found = history.findById(saved.getId()).orElseThrow();

        // Assert
        assertThat(found.getUserId()).isEqualTo(ana.getId());
        assertThat(found.getPreviousStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(found.getNewStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(found.getChangedById()).isEqualTo(ADMIN_ID);
        assertThat(found.getChangedByName()).isEqualTo("Sample Admin");
        assertThat(found.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void findsTheChangesOfOneUserNewestFirst() {
        // Arrange
        User ana = users.save(user("ana@example.com"));
        User luis = users.save(user("luis@example.com"));
        UserStatusChange older = history.save(change(ana, UserStatus.ACTIVE, UserStatus.INACTIVE, "2026-11-01T10:00:00Z"));
        UserStatusChange newer = history.save(change(ana, UserStatus.INACTIVE, UserStatus.ACTIVE, "2026-11-02T10:00:00Z"));
        history.save(change(luis, UserStatus.ACTIVE, UserStatus.INACTIVE, "2026-11-03T10:00:00Z"));
        detach();

        // Act
        List<UserStatusChange> found = history.findByUserIdNewestFirst(ana.getId());

        // Assert
        assertThat(found).extracting(UserStatusChange::getId).containsExactly(newer.getId(), older.getId());
    }

    private static UserStatusChange change(User user, UserStatus from, UserStatus to, String at) {
        return UserStatusChange.restore(UserStatusChangeId.newId(), user.getId(), from, to, ADMIN_ID, "Sample Admin",
                Instant.parse(at));
    }

    private void detach() {
        entityManager.flush();
        entityManager.clear();
    }
}
