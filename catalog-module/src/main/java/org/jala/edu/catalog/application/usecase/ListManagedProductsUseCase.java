package org.jala.edu.catalog.application.usecase;

import java.util.List;
import org.jala.edu.catalog.application.port.in.ListManagedProductsQuery;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.application.usecase.UseCase;

/**
 * Devuelve todos los productos del catálogo para que el administrador o comerciante los gestione.
 * No confundir con la consulta para clientes, que se hace a través de {@code CatalogService}.
 */
public class ListManagedProductsUseCase implements UseCase<ListManagedProductsQuery, List<Product>> {

    private final ProductRepositoryPort productRepository;

    public ListManagedProductsUseCase(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Devuelve los productos del más antiguo al más reciente.
     */
    @Override
    public List<Product> execute(ListManagedProductsQuery query) {
        return productRepository.findAll();
    }
}