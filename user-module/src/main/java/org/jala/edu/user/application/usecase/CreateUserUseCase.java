package org.jala.edu.user.application.usecase;

import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.in.CreateUserCommand;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.EmailAlreadyRegisteredException;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;


public class CreateUserUseCase implements UseCase<CreateUserCommand, UUID> {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;
    private final CurrentUserService currentUserService;

    public CreateUserUseCase(UserRepositoryPort userRepository, PasswordHasher passwordHasher,
                             CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.currentUserService = currentUserService;
    }

    /**
     * An administrator creates a user with any role (FUN-001).
     *
     * @throws org.jala.edu.user.domain.exception.AdminRequiredException if the signed-in user is not an ADMIN
     */
    @Override
    public UUID execute(CreateUserCommand command) {
        AdminOnly.check(currentUserService);
        User newUser = this.userRepository.save(newModel(command));
        return newUser.getId().value();
    }

    /**
     * Validates the command and builds the new user without storing it or checking who asks, so the customer sign-up
     * (#89) reuses the same rules without a session.
     */
    public User newModel(CreateUserCommand command) {
        Email email = new Email(command.email());
        Password password = new Password(command.password());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        User user = User.register(command.firstName(), command.lastName(), email, command.phoneNumber(),
                command.role());
        user.changePasswordHash(passwordHasher.hash(password));
        return user;
    }

}
