package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.RegisterCommerceCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * CU-CAT-01: registra un comercio con nombre, dirección, teléfono y su comerciante dueño. Solo un
 * administrador puede hacerlo, y el dueño debe ser un MERCHANT activo; el comercio queda Deshabilitado.
 * Los datos los valida {@link Commerce}.
 */
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
        accessPolicy.requireAdmin(command.requesterId());
        accessPolicy.requireActiveMerchant(command.merchantId());

        Commerce commerce = Commerce.register(
                command.name(), command.address(), command.phone(), command.merchantId());
        return commerceRepository.save(commerce).getId().value();
    }
}