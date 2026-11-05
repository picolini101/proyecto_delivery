package org.jala.edu.user.application.usecase;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;

/**
 * Looks up one user by id for other modules (#85), whatever their status. An unknown id is not an error here: the
 * answer is simply empty.
 */
public class FindUserUseCase implements UseCase<UUID, Optional<User>> {

    private final UserRepositoryPort userRepository;

    public FindUserUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> execute(UUID userId) {
        return userRepository.findById(new UserId(userId));
    }
}
