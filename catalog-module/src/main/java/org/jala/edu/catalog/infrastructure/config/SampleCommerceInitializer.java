package org.jala.edu.catalog.infrastructure.config;

import java.time.Instant;
import java.util.UUID;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnInMemoryPersistence
public class SampleCommerceInitializer implements InitializingBean {

    private static final UUID SAMPLE_COMMERCE_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000301");

    private final CommerceRepositoryPort commerceRepository;

    public SampleCommerceInitializer(CommerceRepositoryPort commerceRepository) {
        this.commerceRepository = commerceRepository;
    }

    @Override
    public void afterPropertiesSet() {
        CommerceId commerceId = new CommerceId(SAMPLE_COMMERCE_ID);

        if (commerceRepository.findById(commerceId).isEmpty()) {
            Instant now = Instant.now();

            Commerce commerce = Commerce.restore(
                    commerceId,
                    "Sample Commerce",
                    CommerceStatus.ACTIVE,
                    now,
                    now);

            commerceRepository.save(commerce);
        }
    }
}
