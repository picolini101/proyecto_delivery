package org.jala.edu.user.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.domain.model.UserStatusChangeId;
import org.springframework.stereotype.Component;

/**
 * Stores the status changes in memory. Active when {@code delivery.persistence.type=memory};
 * data is lost when the application stops.
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryUserStatusHistoryAdapter extends InMemoryCrudRepository<UserStatusChange, UserStatusChangeId>
        implements UserStatusHistoryPort {

    @Override
    public List<UserStatusChange> findByUserIdNewestFirst(UserId userId) {
        return findAll().reversed().stream().filter(change -> change.getUserId().equals(userId)).toList();
    }
}
