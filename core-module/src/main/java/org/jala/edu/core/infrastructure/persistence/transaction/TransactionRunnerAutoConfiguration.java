package org.jala.edu.core.infrastructure.persistence.transaction;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Gives every module a {@link TransactionRunner} to inject in its use cases' configuration.
 */
@AutoConfiguration
public class TransactionRunnerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TransactionRunner transactionRunner(ObjectProvider<PlatformTransactionManager> transactionManager) {
        return new SpringTransactionRunner(transactionManager);
    }
}
