package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.ChangeCommerceStatusCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

public class ChangeCommerceStatusUseCase
        implements UseCase<ChangeCommerceStatusCommand, UUID> {

    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;

    public ChangeCommerceStatusUseCase(
            CommerceRepositoryPort commerceRepository,
            UserService userService) {
        this.commerceRepository = commerceRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public UUID execute(ChangeCommerceStatusCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        if (command.status() == null) {
            throw new DomainException("Commerce status cannot be null");
        }

        CommerceId id = new CommerceId(command.commerceId());
        Commerce commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new CommerceNotFoundException(id));

        if (command.status() == CommerceStatus.ACTIVE) {
            commerce.activate();
        } else {
            commerce.disable();
        }

        return commerceRepository.save(commerce).getId().value();
    }
}
