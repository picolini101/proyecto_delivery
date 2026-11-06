package org.jala.edu.catalog.application.usecase;

import org.jala.edu.catalog.application.port.in.UpdateProductStatusCommand;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.ProductNotFoundException;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

public class UpdateProductStatusUseCase
        implements UseCase<UpdateProductStatusCommand, Void> {

    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public UpdateProductStatusUseCase(
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public Void execute(UpdateProductStatusCommand command) {
        accessPolicy.requireAuthorized(command.requesterId());

        if (command.status() == null) {
            throw new DomainException("Invalid product status");
        }

        ProductId id = new ProductId(command.productId());

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.updateStatus(command.status());
        productRepository.save(product);

        return null;
    }
}
