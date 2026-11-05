package org.jala.edu.core.infrastructure.persistence.transaction;

import java.util.function.Supplier;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link TransactionRunner} backed by Spring: with {@code delivery.persistence.type=jpa} the work runs in one
 * transaction of the application's transaction manager; in memory mode there is none, so the work just runs.
 */
public class SpringTransactionRunner implements TransactionRunner {

    private final ObjectProvider<PlatformTransactionManager> transactionManager;

    public SpringTransactionRunner(ObjectProvider<PlatformTransactionManager> transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        PlatformTransactionManager manager = transactionManager.getIfAvailable();
        if (manager == null) {
            return work.get();
        }
        return new TransactionTemplate(manager).execute(status -> work.get());
    }
}
