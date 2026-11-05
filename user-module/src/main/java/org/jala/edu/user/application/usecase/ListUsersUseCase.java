package org.jala.edu.user.application.usecase;

import java.util.List;
import java.util.Locale;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.user.application.port.in.ListUsersQuery;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.User;

public class ListUsersUseCase implements UseCase<ListUsersQuery, List<User>> {

    private final UserRepositoryPort userRepository;
    private final CurrentUserService currentUserService;

    public ListUsersUseCase(UserRepositoryPort userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Returns the users that match every filter of the query (all users when none is set), oldest first.
     *
     * @throws org.jala.edu.user.domain.exception.AdminRequiredException if the signed-in user is not an ADMIN
     */
    @Override
    public List<User> execute(ListUsersQuery query) {
        AdminOnly.check(currentUserService);
        return byEmail(query.emailFilter()).stream()
                .filter(user -> query.role() == null || user.getRole() == query.role())
                .filter(user -> query.status() == null || user.getStatus() == query.status())
                .toList();
    }

    private List<User> byEmail(String filter) {
        if (filter == null || filter.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByEmailContaining(filter.strip().toLowerCase(Locale.ROOT));
    }
}
