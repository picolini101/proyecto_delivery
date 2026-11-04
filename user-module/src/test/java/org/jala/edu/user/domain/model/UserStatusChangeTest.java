package org.jala.edu.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class UserStatusChangeTest {

    private final UserId userId = UserId.newId();
    private final UUID adminId = UUID.randomUUID();

    @Test
    void recordsTheStatusesTheAuthorAndTheTime() {
        // Act
        UserStatusChange change = UserStatusChange.record(userId, UserStatus.ACTIVE, UserStatus.INACTIVE, adminId,
                "Sample Admin");

        // Assert
        assertThat(change.getUserId()).isEqualTo(userId);
        assertThat(change.getPreviousStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(change.getNewStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(change.getChangedById()).isEqualTo(adminId);
        assertThat(change.getChangedByName()).isEqualTo("Sample Admin");
        assertThat(change.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsAChangeToTheSameStatus() {
        // Act
        Throwable error = catchThrowable(() ->
                UserStatusChange.record(userId, UserStatus.ACTIVE, UserStatus.ACTIVE, adminId, "Sample Admin"));

        // Assert
        assertThat(error).isInstanceOf(DomainException.class).hasMessage("A status change needs a different status");
    }
}
