package org.jala.edu.catalog.application.usecase;

import java.util.Optional;
import java.util.UUID;
import org.jala.edu.catalog.application.dto.DeleteProductResult;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Elimina un producto (FUN-002, CU-CAT-02). Reglas:
 * <ul>
 *   <li>Solo un administrador puede eliminar (INT-002).</li>
 *   <li>El stock se elimina con el producto; la categoría no se toca (D-09).</li>
 *   <li>Si el comercio Activo se queda sin productos Disponibles, pasa a Deshabilitado (FUN-004).</li>
 * </ul>
 * Orden de las comprobaciones: permisos, existencia y, al final, eliminación.
 */
public class DeleteProductUseCase implements UseCase<DeleteProductCommand, DeleteProductResult> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;
    private final CommerceActivationPolicy activationPolicy;

    public DeleteProductUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
        this.activationPolicy = new CommerceActivationPolicy(productRepository);
    }

    /**
     * @throws ProductNotFoundException si no existe un producto con el id del comando
     */
    @Override
    public DeleteProductResult execute(DeleteProductCommand command) {
        accessPolicy.requireAdmin(command.requesterId());

        ProductId id = new ProductId(command.productId());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.deleteById(id);

        return disableCommerceIfNoAvailableProducts(product.getCommerceId())
                ? DeleteProductResult.DELETED_COMMERCE_DISABLED
                : DeleteProductResult.DELETED;
    }

    /** @return {@code true} solo si el comercio estaba Activo y se deshabilitó. */
    private boolean disableCommerceIfNoAvailableProducts(UUID commerceId) {
        Optional<Commerce> activeCommerce = commerceRepository
                .findById(new CommerceId(commerceId))
                .filter(Commerce::isActive);

        if (activeCommerce.isEmpty() || activationPolicy.hasAvailableProduct(commerceId)) {
            return false;
        }

        Commerce commerce = activeCommerce.get();
        commerce.disable();
        commerceRepository.save(commerce);
        return true;
    }
}