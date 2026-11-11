
package org.jala.edu.catalog.application.usecase;

import java.util.List;
import java.util.Objects;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.CatalogProductPage;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.exception.PageNotFoundException;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;

/**
 * CU-03: catálogo paginado de un comercio visto por el cliente.
 * Un comercio inexistente o deshabilitado no muestra productos.
 * Los productos deshabilitados no se muestran.
 * Los productos agotados se muestran como no disponibles.
 * Devuelve DTOs, no entidades del dominio.
 */
public class ListAvailableProductsUseCase
        implements UseCase<ListAvailableProductsQuery, CatalogProductPage> {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;
    private final int pageSize;

    public ListAvailableProductsUseCase(
            ProductRepositoryPort productRepository,
            CommerceRepositoryPort commerceRepository,
            int pageSize) {
        this.productRepository = productRepository;
        this.commerceRepository = commerceRepository;

        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.pageSize = pageSize;
    }

    @Override
    public CatalogProductPage execute(ListAvailableProductsQuery query) {
        Objects.requireNonNull(query.commerceId(), "Commerce is required");

        if (query.page() < 1) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        boolean commerceActive = commerceRepository.findById(new CommerceId(query.commerceId()))
                .map(commerce -> commerce.isActive())
                .orElse(false);

        if (!commerceActive) {
            return emptyPage(query.page());
        }

        long totalProducts = productRepository.countVisibleProductsByCommerceId(
                query.commerceId(), query.searchText());

        long totalAvailableProducts =
            productRepository.countAvailableProductsByCommerceId(query.commerceId());

        if (totalProducts == 0) {
            return emptyPage(query.page());
        }

        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        if (query.page() > totalPages) {
            throw new PageNotFoundException(query.page());
        }

        int offset = Math.multiplyExact(query.page() - 1, pageSize);

        List<CatalogProduct> products = productRepository
                .findPageOfVisibleProductsByCommerceId(
                        query.commerceId(), query.searchText(), offset, pageSize)
                .stream()
                .map(ListAvailableProductsUseCase::toDto)
                .toList();

        return new CatalogProductPage(
                products,
                query.page(),
                pageSize,
                Math.toIntExact(totalProducts),
                totalPages,
                Math.toIntExact(totalAvailableProducts));
    }

    private CatalogProductPage emptyPage(int page) {
        return new CatalogProductPage(List.of(), page, pageSize, 0, 0, 0);
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
