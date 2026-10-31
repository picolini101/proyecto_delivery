package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.port.in.UpdateProductCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Modifica el nombre, la categoría y el precio de un producto existente (FUN-002, CU-02).
 * Orden de las comprobaciones: permisos, existencia y, por último, reglas del dominio
 * (textos no vacíos y precio mayor que cero, aplicadas por {@link Product}).
 */
public class UpdateProductUseCase implements UseCase<UpdateProductCommand, Void> {

    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public UpdateProductUseCase(ProductRepositoryPort productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    /**
     * @throws ProductNotFoundException si no existe un producto con el id del comando
     */
    @Override
    public Void execute(UpdateProductCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        ProductId id = new ProductId(command.productId());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.update(command.name(), command.category(), command.price());
        productRepository.save(product);
        return null;
    }
}