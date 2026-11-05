package org.jala.edu.core.infrastructure.persistence.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class SpringTransactionRunnerTest {

    private final RecordingTransactionManager manager = new RecordingTransactionManager();

    @Test
    void commitsWhenTheWorkSucceeds() {
        // Arrange
        TransactionRunner transactions = runnerWith(manager);

        // Act
        String result = transactions.inTransaction(() -> "saved");

        // Assert
        assertThat(result).isEqualTo("saved");
        assertThat(manager.events).containsExactly("begin", "commit");
    }

    @Test
    void rollsBackAndRethrowsWhenTheWorkFails() {
        // Arrange
        TransactionRunner transactions = runnerWith(manager);
        IllegalStateException failure = new IllegalStateException("history not saved");

        // Act
        Throwable thrown = catchThrowable(() -> transactions.inTransaction(() -> {
            throw failure;
        }));

        // Assert
        assertThat(thrown).isSameAs(failure);
        assertThat(manager.events).containsExactly("begin", "rollback");
    }

    @Test
    void justRunsTheWorkWithoutATransactionManager() {
        // Arrange (memory mode: no database, no transaction manager)
        TransactionRunner transactions =
                new SpringTransactionRunner(new StaticListableBeanFactory().getBeanProvider(PlatformTransactionManager.class));

        // Act
        Integer result = transactions.inTransaction(() -> 42);

        // Assert
        assertThat(result).isEqualTo(42);
    }

    private static TransactionRunner runnerWith(PlatformTransactionManager manager) {
        StaticListableBeanFactory beans = new StaticListableBeanFactory(Map.of("transactionManager", manager));
        return new SpringTransactionRunner(beans.getBeanProvider(PlatformTransactionManager.class));
    }

    private static class RecordingTransactionManager implements PlatformTransactionManager {

        private final List<String> events = new ArrayList<>();

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            events.add("begin");
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            events.add("commit");
        }

        @Override
        public void rollback(TransactionStatus status) {
            events.add("rollback");
        }
    }
}
