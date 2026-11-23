package org.jala.edu.catalog.application.port.out;

import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;

public interface ProductRepositoryPort extends CrudRepositoryPort<Product, ProductId> {

    /** Productos que pertenecen al comercio indicado. */
    List<Product> findByCommerceId(UUID commerceId);
    List<Product> findPageOfVisibleProductsByCommerceId(UUID commerceId, int offset, int limit);
    long countVisibleProductsByCommerceId(UUID commerceId);
    long countAvailableProductsByCommerceId(UUID commerceId);
    void deleteAllByCommerceId(UUID commerceId);

    /// Página de productos visibles del comercio que coinciden con el texto (vacío = todos)
    List<Product> findPageOfVisibleProductsByCommerceId(
            UUID commerceId, String searchText, int offset, int limit);

    /// Cantidad de productos visibles del comercio que coinciden con el texto (vacío = todos)
    long countVisibleProductsByCommerceId(UUID commerceId, String searchText);
}