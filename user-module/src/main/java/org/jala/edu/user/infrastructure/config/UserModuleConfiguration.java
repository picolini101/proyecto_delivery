package org.jala.edu.user.infrastructure.config;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.application.port.out.UserStatusHistoryPort;
import org.jala.edu.user.application.usecase.ChangeUserStatusUseCase;
import org.jala.edu.user.application.usecase.CheckUserAccessUseCase;
import org.jala.edu.user.application.usecase.CreateUserUseCase;
import org.jala.edu.user.application.usecase.FindUserUseCase;
import org.jala.edu.user.application.usecase.ListActiveUsersByRoleUseCase;
import org.jala.edu.user.application.usecase.ListUserStatusHistoryUseCase;
import org.jala.edu.user.application.usecase.ListUsersUseCase;
import org.jala.edu.user.application.usecase.LoginUseCase;
import org.jala.edu.user.application.usecase.SignUpUseCase;
import org.jala.edu.user.application.usecase.UpdateUserUseCase;
import org.jala.edu.user.infrastructure.adapter.in.contract.SessionCurrentUserService;
import org.jala.edu.user.infrastructure.adapter.in.contract.UserServiceAdapter;
import org.jala.edu.user.infrastructure.adapter.out.security.BCryptPasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfiguration {

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepositoryPort userRepositoryPort, PasswordHasher passwordHasher) {
        return new CreateUserUseCase(userRepositoryPort, passwordHasher);
    }

    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepositoryPort userRepositoryPort,
                                               UserStatusHistoryPort userStatusHistoryPort,
                                               CurrentUserService currentUserService) {
        return new UpdateUserUseCase(userRepositoryPort, userStatusHistoryPort, currentUserService);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepositoryPort userRepositoryPort) {
        return new ListUsersUseCase(userRepositoryPort);
    }

    @Bean
    public ChangeUserStatusUseCase changeUserStatusUseCase(UserRepositoryPort userRepositoryPort,
                                                           UserStatusHistoryPort userStatusHistoryPort,
                                                           CurrentUserService currentUserService) {
        return new ChangeUserStatusUseCase(userRepositoryPort, userStatusHistoryPort, currentUserService);
    }

    @Bean
    public ListUserStatusHistoryUseCase listUserStatusHistoryUseCase(UserRepositoryPort userRepositoryPort,
                                                                     UserStatusHistoryPort userStatusHistoryPort,
                                                                     CurrentUserService currentUserService) {
        return new ListUserStatusHistoryUseCase(userRepositoryPort, userStatusHistoryPort, currentUserService);
    }

    @Bean
    public SignUpUseCase signUpUseCase(CreateUserUseCase createUserUseCase, UserRepositoryPort userRepositoryPort) {
        return new SignUpUseCase(createUserUseCase, userRepositoryPort);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort, PasswordHasher passwordHasher) {
        return new LoginUseCase(userRepositoryPort, passwordHasher);
    }

    /**
     * The {@link org.jala.edu.core.contract.CurrentUserService} contract other modules receive; it replaces core's
     * fallback.
     */
    @Bean
    public SessionCurrentUserService currentUserService(FindUserUseCase findUserUseCase) {
        return new SessionCurrentUserService(findUserUseCase);
    }

    @Bean
    public CheckUserAccessUseCase checkUserAccessUseCase(UserRepositoryPort userRepositoryPort) {
        return new CheckUserAccessUseCase(userRepositoryPort);
    }

    /**
     * The {@link UserService} contract other modules receive; it replaces core's fallback.
     */
    @Bean
    public FindUserUseCase findUserUseCase(UserRepositoryPort userRepositoryPort) {
        return new FindUserUseCase(userRepositoryPort);
    }

    @Bean
    public ListActiveUsersByRoleUseCase listActiveUsersByRoleUseCase(UserRepositoryPort userRepositoryPort) {
        return new ListActiveUsersByRoleUseCase(userRepositoryPort);
    }

    @Bean
    public UserService userService(CheckUserAccessUseCase checkUserAccessUseCase, FindUserUseCase findUserUseCase,
                                   ListActiveUsersByRoleUseCase listActiveUsersByRoleUseCase) {
        return new UserServiceAdapter(checkUserAccessUseCase, findUserUseCase, listActiveUsersByRoleUseCase);
    }

    @Bean
    public SampleUsersLoader sampleUsersLoader(UserRepositoryPort userRepositoryPort, PasswordHasher passwordHasher) {
        return new SampleUsersLoader(userRepositoryPort, passwordHasher);
    }
}
