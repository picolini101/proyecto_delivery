package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.LoginCommand;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.InactiveUserException;
import org.jala.edu.user.domain.exception.InvalidCredentialsException;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;

/**
 * CU-USR-06 · Iniciar sesión (D-10): checks the email and password and returns the user who signs in.
 */
public class LoginUseCase implements UseCase<LoginCommand, User> {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;

    public LoginUseCase(UserRepositoryPort userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * @throws InvalidCredentialsException if the email is unknown or the password does not match
     * @throws InactiveUserException       if the credentials are right but the user is inactive
     */
    @Override
    public User execute(LoginCommand command) {
        Email email;
        Password password;
        try {
            email = new Email(command.email());
            password = new Password(command.password());
        } catch (DomainException e) {
            throw new InvalidCredentialsException();
        }
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isActive()) {
            throw new InactiveUserException();
        }
        return user;
    }
}
