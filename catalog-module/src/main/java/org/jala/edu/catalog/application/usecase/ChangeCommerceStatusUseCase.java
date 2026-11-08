package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.ChangeCommerceStatusCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

public class ChangeCommerceStatusUseCase
        implements UseCase<ChangeCommerceStatusCommand, UUID> {

    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;
    private final ProductRepositoryPort productRepository;
    private final CommerceActivationPolicy activationPolicy;

    static final String NO_AVAILABLE_PRODUCT_MESSAGE =
            "No se puede habilitar el comercio se necesita al menos un producto disponible";

    public ChangeCommerceStatusUseCase(
            CommerceRepositoryPort commerceRepository,
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.commerceRepository = commerceRepository;
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
        this.activationPolicy = new CommerceActivationPolicy(productRepository);
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
            activationPolicy.requireAvailableProduct(command.commerceId());
            commerce.activate();
        } else {
            commerce.disable();
        }

        return commerceRepository.save(commerce).getId().value();
    }
    private void requireAvailableProduct(UUID commerceId) {
        boolean hasAvailableProduct = productRepository.findByCommerceId(commerceId)
                .stream()
                .anyMatch(Product::isPurchasable);

        if (!hasAvailableProduct) {
            throw new DomainException(NO_AVAILABLE_PRODUCT_MESSAGE);
        }
    }
}
