package org.jala.edu.user.application.usecase;

import java.util.Comparator;
import java.util.List;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;

/**
 * Lists the active users of one role, sorted by full name (#85), e.g. so catalog can choose the merchant of a
 * commerce.
 */
public class ListActiveUsersByRoleUseCase implements UseCase<UserRole, List<User>> {

    private final UserRepositoryPort userRepository;

    public ListActiveUsersByRoleUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> execute(UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.isActive() && user.getRole() == role)
                .sorted(Comparator.comparing(User::fullName))
                .toList();
    }
}
