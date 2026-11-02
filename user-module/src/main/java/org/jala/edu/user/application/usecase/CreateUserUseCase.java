package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.user.application.port.in.CreateUserCommand;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.EmailAlreadyRegisteredException;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;

import java.util.UUID;

public class CreateUserUseCase implements UseCase<CreateUserCommand, UUID> {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserUseCase(UserRepositoryPort userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UUID execute(CreateUserCommand command) {
        User newUser = this.userRepository.save(newModel(command));
        return newUser.getId().value();
    }

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
