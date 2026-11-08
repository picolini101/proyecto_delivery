package org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.springframework.stereotype.Component;
import java.util.stream.Stream;

/**
 * Almacena productos en la memoria. Activo cuando {@code delivery.persistence.type=memory};
 * los datos se pierden al detenerse la aplicación
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryProductRepositoryAdapter extends InMemoryCrudRepository<Product, ProductId>
        implements ProductRepositoryPort {

    /**
     * Productos que pertenecen al comercio indicado, ordenados por fecha de creación.
     */
    @Override
    public List<Product> findByCommerceId(UUID commerceId) {
        return findAll().stream()
                .filter(product -> product.getCommerceId().equals(commerceId))
                .toList();
    }
    @Override
    public long countVisibleProductsByCommerceId(UUID commerceId) {
        return countVisibleProductsByCommerceId(commerceId, null);
    }

    @Override
    public long countVisibleProductsByCommerceId(UUID commerceId, String searchText) {
        return visibleProducts(commerceId, searchText).count();
    }

    @Override
    public long countAvailableProductsByCommerceId(UUID commerceId) {
        return findByCommerceId(commerceId).stream()
                .filter(product -> product.getStatus() == ProductStatus.AVAILABLE)
                .filter(product -> product.getStock() > 0)
                .count();
    }
    private Stream<Product> visibleProducts(UUID commerceId, String searchText) {
        return findByCommerceId(commerceId).stream()
                .filter(product -> product.getStatus() != ProductStatus.DISABLED)
                .filter(product -> product.matches(searchText));
    }

    @Override
    public List<Product> findPageOfVisibleProductsByCommerceId(
            UUID commerceId, String searchText, int offset, int limit) {
        return visibleProducts(commerceId, searchText)
                .skip(offset)
                .limit(limit)
                .toList();
    }

    @Override
    public List<Product> findPageOfVisibleProductsByCommerceId(UUID commerceId, int offset, int limit) {
        return findPageOfVisibleProductsByCommerceId(commerceId, null, offset, limit);
    }

}