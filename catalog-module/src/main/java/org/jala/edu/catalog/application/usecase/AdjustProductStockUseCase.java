package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.port.in.AdjustProductStockCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

/**
 * Ajusta el stock de un producto existente
 */
public class AdjustProductStockUseCase implements UseCase<AdjustProductStockCommand, Void> {

    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public AdjustProductStockUseCase(ProductRepositoryPort productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public Void execute(AdjustProductStockCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        ProductId id = new ProductId(command.productId());
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.adjustStock(command.quantity());
        productRepository.save(product);
        return null;
    }
}