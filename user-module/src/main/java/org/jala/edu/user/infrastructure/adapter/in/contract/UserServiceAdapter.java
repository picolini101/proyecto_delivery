package org.jala.edu.user.infrastructure.adapter.in.contract;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.contract.UserSummary;
import org.jala.edu.user.application.port.in.CheckUserAccessQuery;
import org.jala.edu.user.application.usecase.CheckUserAccessUseCase;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.application.usecase.ListActiveUsersByRoleUseCase;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserRole;

/**
 * Inbound adapter that serves the {@link UserService} contract to other modules through this module's use cases.
 * The contract's roles are translated by name, so the user domain keeps its own {@link UserRole}; users leave the
 * module only as {@link UserSummary}.
 */
public class UserServiceAdapter implements UserService {

    private final CheckUserAccessUseCase checkUserAccessUseCase;
    private final FindUserUseCase findUserUseCase;
    private final ListActiveUsersByRoleUseCase listActiveUsersByRoleUseCase;

    public UserServiceAdapter(CheckUserAccessUseCase checkUserAccessUseCase, FindUserUseCase findUserUseCase,
                              ListActiveUsersByRoleUseCase listActiveUsersByRoleUseCase) {
        this.checkUserAccessUseCase = checkUserAccessUseCase;
        this.findUserUseCase = findUserUseCase;
        this.listActiveUsersByRoleUseCase = listActiveUsersByRoleUseCase;
    }

    @Override
    public boolean isActiveWithRole(UUID userId, org.jala.edu.core.contract.UserRole... roles) {
        Set<UserRole> userRoles = Arrays.stream(roles)
                .map(role -> UserRole.valueOf(role.name()))
                .collect(Collectors.toSet());
        return checkUserAccessUseCase.execute(new CheckUserAccessQuery(userId, userRoles));
    }

    @Override
    public Optional<UserSummary> findUser(UUID userId) {
        return findUserUseCase.execute(userId).map(UserServiceAdapter::toSummary);
    }

    @Override
    public List<UserSummary> findActiveByRole(org.jala.edu.core.contract.UserRole role) {
        return listActiveUsersByRoleUseCase.execute(UserRole.valueOf(role.name())).stream()
                .map(UserServiceAdapter::toSummary)
                .toList();
    }

    private static UserSummary toSummary(User user) {
        return new UserSummary(user.getId().value(), user.getFirstName(), user.getLastName(),
                user.getEmail().value(), user.getPhoneNumber(),
                org.jala.edu.core.contract.UserRole.valueOf(user.getRole().name()), user.isActive());
    }
}
