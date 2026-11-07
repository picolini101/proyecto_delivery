package org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.springframework.stereotype.Component;

/**
 * Almacena productos en la memoria. Activo cuando {@code delivery.persistence.type=memory};
 * los datos se pierden al detenerse la aplicación
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryProductRepositoryAdapter extends InMemoryCrudRepository<Product, ProductId>
        implements ProductRepositoryPort {

    /**
     * Productos que pertenecen al comercio indicado. Se apoya en {@code stream()} de la clase base.
     */
    @Override
    public List<Product> findByCommerceId(UUID commerceId) {
        return stream()
                .filter(product -> product.getCommerceId().equals(commerceId))
                .toList();
    }
}