package org.jala.edu.catalog.application.usecase;

import java.util.List;
import java.util.Objects;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.core.application.usecase.UseCase;

/**
 * CU-03: catálogo de un comercio visto por el cliente (nombre, categoría y precio vigente).
 *  Un comercio inexistente o deshabilitado no muestra productos
 *  Los productos deshabilitados no se muestran
 *  Los productos agotados (estado OUT_OF_STOCK o stock 0) se muestran como no disponibles
 * Devuelve DTOs no entidades del dominio
 */
public class ListAvailableProductsUseCase
        implements UseCase<ListAvailableProductsQuery, List<CatalogProduct>> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;

    public ListAvailableProductsUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;
    }

    @Override
    public List<CatalogProduct> execute(ListAvailableProductsQuery query) {
        Objects.requireNonNull(query.commerceId(), "Commerce is required");

        boolean commerceActive = commerceRepository.findById(new CommerceId(query.commerceId()))
                .map(commerce -> commerce.isActive())
                .orElse(false);

        if (!commerceActive) {
            return List.of();
        }

        return productRepository.findAll().stream()
                .filter(product -> product.getCommerceId().equals(query.commerceId()))
                .filter(product -> product.getStatus() != ProductStatus.DISABLED)
                .map(ListAvailableProductsUseCase::toDto)
                .toList();
    }

    private static CatalogProduct toDto(Product product) {
        boolean available = product.isAvailable() && product.getStock() > 0;
        return new CatalogProduct(
                product.getId().value(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                available);
    }
}