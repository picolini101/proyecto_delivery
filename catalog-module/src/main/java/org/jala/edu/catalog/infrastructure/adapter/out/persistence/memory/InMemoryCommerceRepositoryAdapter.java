package org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory;

import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnInMemoryPersistence
public class InMemoryCommerceRepositoryAdapter
        extends InMemoryCrudRepository<Commerce, CommerceId>
        implements CommerceRepositoryPort {
}

