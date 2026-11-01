package org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory;

import java.util.UUID;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.domain.model.Courier;
import org.springframework.stereotype.Component;

/**
 * Stores the delivery drivers in memory
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryCourierRepositoryAdapter
        extends InMemoryCrudRepository<Courier, UUID>
        implements CourierRepositoryPort {
}