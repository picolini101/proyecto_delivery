package org.jala.edu.user.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.user.application.port.in.ChangePasswordCommand;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.exception.UserNotFoundException;
import org.jala.edu.user.domain.exception.WrongCurrentPasswordException;
import org.jala.edu.user.domain.model.Password;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;

/**
 * CU-USR-09 · Cambiar contraseña (#90): a user replaces their own password after typing the current one.
 */
public class ChangePasswordUseCase implements UseCase<ChangePasswordCommand, Void> {

    private final UserRepositoryPort userRepository;
    private final PasswordHasher passwordHasher;

    public ChangePasswordUseCase(UserRepositoryPort userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * @throws WrongCurrentPasswordException if the current password does not match
     * @throws DomainException               if the new password is shorter than {@link Password#MIN_LENGTH}
     */
    @Override
    public Void execute(ChangePasswordCommand command) {
        UserId id = new UserId(command.userId());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (!matchesCurrent(command.currentPassword(), user)) {
            throw new WrongCurrentPasswordException();
        }
        user.changePasswordHash(passwordHasher.hash(new Password(command.newPassword())));
        userRepository.save(user);
        return null;
    }

    private boolean matchesCurrent(String typed, User user) {
        try {
            return user.hasPassword() && passwordHasher.matches(new Password(typed), user.getPasswordHash());
        } catch (DomainException e) {
            return false;
        }
    }
}
