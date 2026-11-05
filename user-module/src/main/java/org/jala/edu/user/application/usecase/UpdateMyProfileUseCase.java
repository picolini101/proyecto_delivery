package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.in.UpdateMyProfileCommand;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;

/**
 * CU-USR-10 · Editar mi perfil (RF-1.08): any signed-in user changes their own name and phone. The user always comes
 * from the session, never from the command, so nobody can edit someone else's profile this way.
 */
public class UpdateMyProfileUseCase implements UseCase<UpdateMyProfileCommand, Void> {

    private final UserRepositoryPort userRepository;
    private final CurrentUserService currentUserService;

    public UpdateMyProfileUseCase(UserRepositoryPort userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * @throws org.jala.edu.core.contract.NotAuthenticatedException if nobody has signed in
     * @throws org.jala.edu.core.domain.exception.DomainException   if a name is blank or the phone is invalid
     */
    @Override
    public Void execute(UpdateMyProfileCommand command) {
        UserId id = new UserId(currentUserService.currentUser().id());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.updateProfile(command.firstName(), command.lastName(), command.phoneNumber());
        userRepository.save(user);
        return null;
    }
}
