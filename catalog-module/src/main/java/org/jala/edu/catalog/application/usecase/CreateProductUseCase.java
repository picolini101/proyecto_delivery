package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

/**
 *Registra un producto en el catálogo de un comercio. Solo un administrador o comerciante activo puede realizar esta acción
 * reglas propias del producto (nombre, categoría, precio) son aplicadas por {@link Product}.
 */
public class CreateProductUseCase implements UseCase<CreateProductCommand, UUID> {

    private final ProductRepositoryPort productRepository;
    private final UserService userService;

    public CreateProductUseCase(ProductRepositoryPort productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    @Override
    public UUID execute(CreateProductCommand command) {
        requireAuthorized(command.requesterId());
        Product product = Product.register(
                command.commerceId(), command.name(), command.category(), command.price());
        return productRepository.save(product).getId().value();
    }

    private void requireAuthorized(UUID requesterId) {
        if (!userService.isActiveWithRole(requesterId, UserRole.ADMIN, UserRole.MERCHANT)) {
            throw new DomainException("User not authorized for this operation");
        }
    }
}