package org.jala.edu.catalog.application.port.out;

import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;

public interface ProductRepositoryPort extends CrudRepositoryPort<Product, ProductId> {

    /** Productos que pertenecen al comercio indicado. */
    List<Product> findByCommerceId(UUID commerceId);
}