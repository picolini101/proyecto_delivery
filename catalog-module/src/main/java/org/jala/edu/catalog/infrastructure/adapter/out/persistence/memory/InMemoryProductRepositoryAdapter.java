package org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory;

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
}