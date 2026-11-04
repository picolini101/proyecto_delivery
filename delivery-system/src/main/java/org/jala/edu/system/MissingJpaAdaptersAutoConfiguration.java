package org.jala.edu.system;

import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierRepositoryAdapter;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryCartRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Temporary bridge for {@code delivery.persistence.type=jpa}: a repository port whose module has no JPA adapter yet
 * keeps its in-memory adapter, with a WARN, so the application starts with MySQL while each team adds its own.
 * Auto-configuration runs after the modules' configuration, so a team's JPA adapter always replaces the bridge.
 * Remove each entry once its module provides the JPA adapter.
 */
@AutoConfiguration
@ConditionalOnJpaPersistence
public class MissingJpaAdaptersAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MissingJpaAdaptersAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CartRepositoryPort inMemoryCartRepositoryBridge() {
        warnInMemory(CartRepositoryPort.class);
        return new InMemoryCartRepositoryAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public CourierRepositoryPort inMemoryCourierRepositoryBridge() {
        warnInMemory(CourierRepositoryPort.class);
        return new InMemoryCourierRepositoryAdapter();
    }

    private static void warnInMemory(Class<?> port) {
        log.warn("No JPA adapter for {}; its data stays in memory and is lost on exit", port.getSimpleName());
    }
}
