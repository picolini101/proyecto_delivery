
package org.jala.edu.catalog.application.usecase;

import java.util.List;
import org.jala.edu.catalog.application.port.in.DeleteCommerceCommand;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserService;

public class DeleteCommerceUseCase implements UseCase<DeleteCommerceCommand, Void> {

    private final CommerceRepositoryPort commerceRepository;
    private final ProductRepositoryPort productRepository;
    private final CatalogAccessPolicy accessPolicy;

    public DeleteCommerceUseCase(
            CommerceRepositoryPort commerceRepository,
            ProductRepositoryPort productRepository,
            UserService userService) {
        this.commerceRepository = commerceRepository;
        this.productRepository = productRepository;
        this.accessPolicy = new CatalogAccessPolicy(userService);
    }

    @Override
    public Void execute(DeleteCommerceCommand command) {
        accessPolicy.requireAdmin(command.requesterId());

        CommerceId commerceId = new CommerceId(command.commerceId());
        Commerce commerce = commerceRepository.findById(commerceId)
                .orElseThrow(() -> new CommerceNotFoundException(commerceId));

        List<Product> products = productRepository.findByCommerceId(commerceId.value());

        try {
            productRepository.deleteAllByCommerceId(commerceId.value());
            commerceRepository.deleteById(commerceId);
        } catch (RuntimeException exception) {
            restoreProducts(products, exception);
            restoreCommerce(commerce, exception);
            throw exception;
        }

        return null;
    }

    private void restoreProducts(List<Product> products, RuntimeException originalException) {
        for (Product product : products) {
            try {
                productRepository.save(product);
            } catch (RuntimeException rollbackException) {
                originalException.addSuppressed(rollbackException);
            }
        }
    }

    private void restoreCommerce(Commerce commerce, RuntimeException originalException) {
        try {
            commerceRepository.save(commerce);
        } catch (RuntimeException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
