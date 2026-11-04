package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.in.UpdateUserCommand;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.domain.exception.LastActiveAdminException;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserStatus;

public class UpdateUserUseCase implements UseCase<UpdateUserCommand, Void> {

    private final UserRepositoryPort userRepository;
    private final UserStatusHistoryPort statusHistory;
    private final CurrentUserService currentUserService;

    public UpdateUserUseCase(UserRepositoryPort userRepository, UserStatusHistoryPort statusHistory,
                             CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.statusHistory = statusHistory;
        this.currentUserService = currentUserService;
    }

    /**
     * @throws UserNotFoundException    if no user has the command's id
     * @throws LastActiveAdminException if it would demote or deactivate the only active ADMIN
     */
    @Override
    public Void execute(UpdateUserCommand command) {
        UserId id = new UserId(command.id());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        ActiveAdminRule.check(user, command.role(), command.status(), userRepository);
        UserStatus previousStatus = user.getStatus();
        applyChanges(user, command);
        userRepository.save(user);
        if (user.getStatus() != previousStatus) {
            StatusHistory.record(user, previousStatus, statusHistory, currentUserService);
        }
        return null;
    }

    private void applyChanges(User user, UpdateUserCommand command) {
        user.updateProfile(command.firstName(), command.lastName(), command.phoneNumber());
        if (command.role() != user.getRole()) {
            user.changeRole(command.role());
        }
        if (command.status() != user.getStatus()) {
            if (command.status() == UserStatus.ACTIVE) {
                user.activate();
            } else {
                user.deactivate();
            }
        }
    }
}
