package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.dto.EnableCommerceResult;
import org.jala.edu.catalog.application.port.in.EnableCommerceCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * CU-CAT-04: habilita un comercio Deshabilitado. Reglas:
 *  Solo un administrador puede habilitar
 *  El comercio necesita al menos un producto Disponible con stock (FUN-004 / FUN-005)
 *  Si ya está Activo no cambia nada y se informa {@code ALREADY_ENABLED}
 */
public class EnableCommerceUseCase
        implements UseCase<EnableCommerceCommand, EnableCommerceResult> {

    private final CommerceRepositoryPort commerceRepository;
    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public EnableCommerceUseCase(
            CommerceRepositoryPort commerceRepository,
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.commerceRepository = commerceRepository;
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public EnableCommerceResult execute(EnableCommerceCommand command) {
        accessPolicy.requireAdmin(command.requesterId());

        CommerceId id = new CommerceId(command.commerceId());
        Commerce commerce = commerceRepository.findById(id)
                .orElseThrow(() -> new CommerceNotFoundException(id));

        if (commerce.isActive()) {
            return EnableCommerceResult.ALREADY_ENABLED;
        }

        requireAvailableProduct(command);

        commerce.activate();
        commerceRepository.save(commerce);
        return EnableCommerceResult.ENABLED;
    }

    private void requireAvailableProduct(EnableCommerceCommand command) {
        boolean hasAvailableProduct = productRepository.findByCommerceId(command.commerceId())
                .stream()
                .anyMatch(Product::isPurchasable);

        if (!hasAvailableProduct) {
            throw new DomainException(
                    "A commerce needs at least one available product to be enabled");
        }
    }
}