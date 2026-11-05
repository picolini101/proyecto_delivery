package org.jala.edu.core.application.port.out;

import java.util.function.Supplier;

/**
 * Runs a use case's writes as one unit (RNF-1.05): with MySQL they all commit or all roll back; in memory they simply
 * run. Use it when a use case saves more than once, e.g. a user and their status history, so a failure halfway never
 * leaves half of the change stored. The use case stays free of Spring: it only knows this port.
 */
public interface TransactionRunner {

    /**
     * @return what {@code work} returns; a runtime exception thrown by {@code work} rolls back and is rethrown
     */
    <T> T inTransaction(Supplier<T> work);

    /**
     * Runs the work without a transaction, for unit tests and in-memory adapters.
     */
    static TransactionRunner direct() {
        return new TransactionRunner() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return work.get();
            }
        };
    }
}
