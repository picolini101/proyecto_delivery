package org.jala.edu.catalog.application.usecase;

import java.util.List;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.core.application.usecase.UseCase;

/**
 * Lista los comercios creados, del más antiguo al más reciente. Alimenta el selector del formulario
 * de producto, la pantalla de administración y el catálogo del cliente (solo Activos).
 */
public class ListCommercesUseCase
        implements UseCase<ListCommercesQuery, List<CommerceSummary>> {

    private final CommerceRepositoryPort commerceRepository;
    private final ProductRepositoryPort productRepository;

    public ListCommercesUseCase(
            CommerceRepositoryPort commerceRepository,
            ProductRepositoryPort productRepository) {
        this.commerceRepository = commerceRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CommerceSummary> execute(ListCommercesQuery query) {
        return commerceRepository.findAll().stream()
                .filter(commerce -> !query.onlyActive() || commerce.isActive())
                .map(this::toSummary)
                .toList();
    }

    private CommerceSummary toSummary(Commerce commerce) {
        return new CommerceSummary(
                commerce.getId().value(),
                commerce.getName(),
                commerce.getAddress(),
                commerce.getPhone().value(),
                commerce.getStatus(),
                productRepository.findByCommerceId(commerce.getId().value()).size());
    }
}