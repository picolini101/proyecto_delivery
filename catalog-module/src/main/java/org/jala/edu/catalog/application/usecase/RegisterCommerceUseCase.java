package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.RegisterCommerceCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

public class RegisterCommerceUseCase implements UseCase<RegisterCommerceCommand, UUID> {

    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;

    public RegisterCommerceUseCase(
            CommerceRepositoryPort commerceRepository,
            UserService userService) {
        this.commerceRepository = commerceRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public UUID execute(RegisterCommerceCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        Commerce commerce = Commerce.register(command.name());
        return commerceRepository.save(commerce).getId().value();
    }
}
