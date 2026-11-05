package org.jala.edu.user.application.usecase;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.exception.AdminRequiredException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatusChange;

/**
 * CU-USR-07 (#82): the status changes of one user, newest first. Only an administrator may see them.
 */
public class ListUserStatusHistoryUseCase implements UseCase<UUID, List<UserStatusChange>> {

    private final UserRepositoryPort userRepository;
    private final UserStatusHistoryPort statusHistory;
    private final CurrentUserService currentUserService;

    public ListUserStatusHistoryUseCase(UserRepositoryPort userRepository, UserStatusHistoryPort statusHistory,
                                        CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.statusHistory = statusHistory;
        this.currentUserService = currentUserService;
    }

    /**
     * @throws AdminRequiredException if the signed-in user is not an ADMIN
     * @throws UserNotFoundException  if no user has the given id
     */
    @Override
    public List<UserStatusChange> execute(UUID userId) {
        AdminOnly.check(currentUserService);
        UserId id = new UserId(userId);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        return statusHistory.findByUserIdNewestFirst(id);
    }
}
