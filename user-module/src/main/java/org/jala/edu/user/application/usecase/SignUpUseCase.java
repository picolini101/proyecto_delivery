package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.user.application.port.in.CreateUserCommand;
import org.jala.edu.user.application.port.in.SignUpCommand;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;

/**
 * CU-USR-08 · Registrarse como cliente (#89): anyone can create an active CUSTOMER account from the login screen. Other
 * roles are still created only by an administrator. Returns the new user so the screen can sign them in.
 */
public class SignUpUseCase implements UseCase<SignUpCommand, User> {

    private final CreateUserUseCase createUserUseCase;
    private final UserRepositoryPort userRepository;

    public SignUpUseCase(CreateUserUseCase createUserUseCase, UserRepositoryPort userRepository) {
        this.createUserUseCase = createUserUseCase;
        this.userRepository = userRepository;
    }

    /**
     * @throws org.jala.edu.user.domain.exception.EmailAlreadyRegisteredException if the email already has an account
     */
    @Override
    public User execute(SignUpCommand command) {
        CreateUserCommand customer = new CreateUserCommand(command.firstName(), command.lastName(), command.email(),
                command.phoneNumber(), UserRole.CUSTOMER, command.password());
        return userRepository.save(createUserUseCase.newModel(customer));
    }
}
