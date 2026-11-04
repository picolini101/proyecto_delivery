package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.domain.model.UserStatusChangeId;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
class UserStatusHistoryPersistenceAdapter
        extends JpaCrudRepositoryAdapter<UserStatusChange, UserStatusChangeId, UserStatusChangeJpaEntity, String>
        implements UserStatusHistoryPort {

    private final UserStatusChangeJpaRepository repository;
    private final UserStatusChangePersistenceMapper mapper = new UserStatusChangePersistenceMapper();

    UserStatusHistoryPersistenceAdapter(UserStatusChangeJpaRepository repository) {
        super(repository, new UserStatusChangePersistenceMapper());
        this.repository = repository;
    }

    @Override
    public List<UserStatusChange> findByUserIdNewestFirst(UserId userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId.toString()).stream().map(mapper::toDomain).toList();
    }
}
