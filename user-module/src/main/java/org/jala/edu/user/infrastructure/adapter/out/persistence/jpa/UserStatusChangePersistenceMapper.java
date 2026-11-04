package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import java.util.UUID;
import org.jala.edu.core.infrastructure.persistence.jpa.PersistenceMapper;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatus;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.domain.model.UserStatusChangeId;

final class UserStatusChangePersistenceMapper
        implements PersistenceMapper<UserStatusChange, UserStatusChangeId, UserStatusChangeJpaEntity, String> {

    @Override
    public UserStatusChangeJpaEntity toEntity(UserStatusChange change) {
        return new UserStatusChangeJpaEntity(
                toEntityId(change.getId()),
                change.getUserId().toString(),
                change.getPreviousStatus().name(),
                change.getNewStatus().name(),
                change.getChangedById().toString(),
                change.getChangedByName(),
                change.getCreatedAt());
    }

    @Override
    public UserStatusChange toDomain(UserStatusChangeJpaEntity entity) {
        return UserStatusChange.restore(
                UserStatusChangeId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                UserStatus.valueOf(entity.getPreviousStatus()),
                UserStatus.valueOf(entity.getNewStatus()),
                UUID.fromString(entity.getChangedById()),
                entity.getChangedByName(),
                entity.getCreatedAt());
    }

    @Override
    public String toEntityId(UserStatusChangeId id) {
        return id.toString();
    }
}
