package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.dto.DeleteProductResult;
import org.jala.edu.catalog.application.port.in.DeleteProductCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Elimina un producto (FUN-002, CU-CAT-02). Solo un administrador puede eliminar (INT-002).
 * Orden de las comprobaciones: permisos, existencia y, al final, eliminación.
 */
public class DeleteProductUseCase implements UseCase<DeleteProductCommand, DeleteProductResult> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;
    private final CatalogAccessPolicy accessPolicy;

    public DeleteProductUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    /**
     * @throws ProductNotFoundException si no existe un producto con el id del comando
     */
    @Override
    public DeleteProductResult execute(DeleteProductCommand command) {
        accessPolicy.requireAdmin(command.requesterId());

        ProductId id = new ProductId(command.productId());
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.deleteById(id);
        return DeleteProductResult.DELETED;
    }
}