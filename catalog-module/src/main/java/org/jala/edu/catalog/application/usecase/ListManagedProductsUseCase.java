package org.jala.edu.catalog.application.usecase;

import java.util.List;
import java.util.Optional;
import org.jala.edu.catalog.application.dto.ManagedProduct;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
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
        Optional<Commerce> commerce = commerceRepository
                .findById(new CommerceId(product.getCommerceId()));

        boolean commerceUnavailable = commerce
                .map(found -> !found.isActive())
                .orElse(true);
        String commerceName = commerce.map(Commerce::getName).orElse("Unknown");

        String availability;
        if (commerceUnavailable || product.getStatus() == ProductStatus.DISABLED) {
            availability = "No disponible";
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK
                || product.getStock() == 0) {
            availability = "Agotado";
        } else {
            availability = "Disponible";
        }

        return new ManagedProduct(product, commerceName, availability);
    }
}