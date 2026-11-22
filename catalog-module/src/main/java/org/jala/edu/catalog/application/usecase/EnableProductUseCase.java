package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.port.in.EnableProductCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Habilita un producto que se encuentra deshabilitado.
 * Solo permite la operación a administradores y comerciantes activos.
 */
public class EnableProductUseCase implements UseCase<EnableProductCommand, Void> {

    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public EnableProductUseCase(
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public Void execute(EnableProductCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        ProductId id = new ProductId(command.productId());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.enable();
        productRepository.save(product);

        return null;
    }
}
