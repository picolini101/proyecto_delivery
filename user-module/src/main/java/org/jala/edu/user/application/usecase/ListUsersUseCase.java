package org.jala.edu.user.application.usecase;

import java.util.List;
import java.util.Locale;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.user.application.port.in.ListUsersQuery;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.User;

public class ListUsersUseCase implements UseCase<ListUsersQuery, List<User>> {

    private final UserRepositoryPort userRepository;

    public ListUsersUseCase(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the users whose email contains the query's filter (every user when it is blank), oldest first.
     */
    @Override
    public List<User> execute(ListUsersQuery query) {
        String filter = query.emailFilter();
        if (filter == null || filter.isBlank()) {
            return userRepository.findAll();
        }
        return userRepository.findByEmailContaining(filter.strip().toLowerCase(Locale.ROOT));
    }
}
