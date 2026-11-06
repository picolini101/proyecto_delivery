package org.jala.edu.catalog.application.usecase;

import java.util.List;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.core.application.usecase.UseCase;

public class ListManagedProductsUseCase
        implements UseCase<ListManagedProductsQuery, List<ManagedProduct>> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;

    public ListManagedProductsUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;
    }

    @Override
    public List<ManagedProduct> execute(ListManagedProductsQuery query) {
        return productRepository.findAll().stream()
                .map(this::toManagedProduct)
                .toList();
    }

    private ManagedProduct toManagedProduct(Product product) {
        String availability;

        boolean commerceUnavailable = commerceRepository
                .findById(new CommerceId(product.getCommerceId()))
                .map(commerce -> !commerce.isActive())
                .orElse(true);

        if (commerceUnavailable || product.getStatus() == ProductStatus.DISABLED) {
            availability = "No disponible";
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK
                || product.getStock() == 0) {
            availability = "Agotado";
        } else {
            availability = "Disponible";
        }

        return new ManagedProduct(product, availability);
    }
}
