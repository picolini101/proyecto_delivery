package org.jala.edu.user.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

/**
 * FUN-003 (#82): one activation or deactivation of a user, with who did it. The change time is
 * {@link #getCreatedAt()}. A change is never edited after it is recorded.
 */
@Getter
@ToString(callSuper = true)
public class UserStatusChange extends BaseModel<UserStatusChangeId> {

    private final UserId userId;
    private final UserStatus previousStatus;
    private final UserStatus newStatus;
    private final UUID changedById;
    private final String changedByName;

    private UserStatusChange(UserStatusChangeId id, UserId userId, UserStatus previousStatus, UserStatus newStatus,
                             UUID changedById, String changedByName, Instant createdAt) {
        super(id, createdAt, createdAt);
        this.userId = Objects.requireNonNull(userId, "User id is required");
        this.previousStatus = Objects.requireNonNull(previousStatus, "Previous status is required");
        this.newStatus = Objects.requireNonNull(newStatus, "New status is required");
        if (previousStatus == newStatus) {
            throw new DomainException("A status change needs a different status");
        }
        this.changedById = Objects.requireNonNull(changedById, "Author id is required");
        this.changedByName = Objects.requireNonNull(changedByName, "Author name is required");
    }

    /**
     * Records a new change made now by {@code changedById} ({@code changedByName} is kept as it was then).
     */
    public static UserStatusChange record(UserId userId, UserStatus previousStatus, UserStatus newStatus,
                                          UUID changedById, String changedByName) {
        return new UserStatusChange(UserStatusChangeId.newId(), userId, previousStatus, newStatus, changedById,
                changedByName, now());
    }

    /**
     * Rebuilds a stored change. Only persistence adapters should call this.
     */
    public static UserStatusChange restore(UserStatusChangeId id, UserId userId, UserStatus previousStatus,
                                           UserStatus newStatus, UUID changedById, String changedByName,
                                           Instant createdAt) {
        return new UserStatusChange(id, userId, previousStatus, newStatus, changedById, changedByName, createdAt);
    }
}
