package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.port.in.DisableProductCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Deshabilita un producto sin eliminarlo del catálogo (FUN-002, CU-CAT-02).
 * Solo permite la operación a administradores y comerciantes activos.
 */
public class DisableProductUseCase implements UseCase<DisableProductCommand, Void> {

    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public DisableProductUseCase(
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    /**
     * @throws ProductNotFoundException si no existe el producto solicitado
     */
    @Override
    public Void execute(DisableProductCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        ProductId id = new ProductId(command.productId());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.disable();
        productRepository.save(product);

        return null;
    }
}