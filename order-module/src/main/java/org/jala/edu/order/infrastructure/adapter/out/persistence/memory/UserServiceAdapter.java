package org.jala.edu.order.infrastructure.adapter.out.persistence.memory;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.UserGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserServiceAdapter implements UserGateway {
    private final UserService userService;
    public UserServiceAdapter(UserService userService) {
        this.userService = userService;
    }
    @Override
    public boolean isAuthorizedCustomer(UUID customerId) {
        return userService.isActiveWithRole(customerId, UserRole.CUSTOMER);
    }
}
