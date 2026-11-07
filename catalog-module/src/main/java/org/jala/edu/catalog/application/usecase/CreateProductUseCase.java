package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Registra un producto en el catálogo de un comercio existente. Solo un administrador o comerciante
 * activo puede hacerlo. El comercio puede estar Deshabilitado: así se crea el primer producto que
 * permite habilitarlo. Las reglas propias del producto las aplica {@link Product}.
 */
public class CreateProductUseCase implements UseCase<CreateProductCommand, UUID> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;

    public CreateProductUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public UUID execute(CreateProductCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());
        requireExistingCommerce(command.commerceId());

        Product product = Product.register(
                command.commerceId(), command.name(), command.category(), command.price());
        return productRepository.save(product).getId().value();
    }

    private void requireExistingCommerce(UUID commerceId) {
        if (commerceId == null) {
            throw new DomainException("Commerce is required");
        }
        CommerceId id = new CommerceId(commerceId);
        if (!commerceRepository.existsById(id)) {
            throw new CommerceNotFoundException(id);
        }
    }
}