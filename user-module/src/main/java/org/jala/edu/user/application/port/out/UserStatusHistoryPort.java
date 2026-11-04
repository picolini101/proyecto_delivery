package org.jala.edu.user.application.port.out;

import java.util.List;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatusChange;
import org.jala.edu.user.domain.model.UserStatusChangeId;

/**
 * Stores the status changes of the users (#82).
 */
public interface UserStatusHistoryPort extends CrudRepositoryPort<UserStatusChange, UserStatusChangeId> {

    /**
     * @return the status changes of {@code userId}, newest first
     */
    List<UserStatusChange> findByUserIdNewestFirst(UserId userId);
}
