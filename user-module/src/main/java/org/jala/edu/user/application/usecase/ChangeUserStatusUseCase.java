package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.in.ChangeUserStatusCommand;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.exception.LastActiveAdminException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatus;

/**
 * FUN-003: users are deactivated, never deleted, because other modules keep their id in orders, deliveries and
 * payments.
 */
public class ChangeUserStatusUseCase implements UseCase<ChangeUserStatusCommand, Void> {

    private final UserRepositoryPort userRepository;
    private final UserStatusHistoryPort statusHistory;
    private final CurrentUserService currentUserService;

    public ChangeUserStatusUseCase(UserRepositoryPort userRepository, UserStatusHistoryPort statusHistory,
                                   CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.statusHistory = statusHistory;
        this.currentUserService = currentUserService;
    }

    /**
     * @throws org.jala.edu.user.domain.exception.AdminRequiredException if the signed-in user is not an ADMIN
     * @throws UserNotFoundException    if no user has the command's id
     * @throws LastActiveAdminException if it would deactivate the only active ADMIN
     */
    @Override
    public Void execute(ChangeUserStatusCommand command) {
        AdminOnly.check(currentUserService);
        UserId id = new UserId(command.id());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (command.status() == user.getStatus()) {
            return null;
        }
        ActiveAdminRule.check(user, user.getRole(), command.status(), userRepository);
        UserStatus previousStatus = user.getStatus();
        if (command.status() == UserStatus.ACTIVE) {
            user.activate();
        } else {
            user.deactivate();
        }
        userRepository.save(user);
        StatusHistory.record(user, previousStatus, statusHistory, currentUserService);
        return null;
    }
}
