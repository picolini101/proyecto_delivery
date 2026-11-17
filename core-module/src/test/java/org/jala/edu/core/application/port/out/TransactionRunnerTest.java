package org.jala.edu.core.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionRunnerTest {

    private final TransactionRunner runner = TransactionRunner.direct();

    @Test
    void directRunsTheWorkAndReturnsItsResult() {
        // Arrange
        List<String> saved = new ArrayList<>();

        // Act
        String result = runner.inTransaction(() -> {
            saved.add("user");
            return "done";
        });

        // Assert
        assertThat(result).isEqualTo("done");
        assertThat(saved).containsExactly("user");
    }

    @Test
    void directRethrowsTheExceptionOfTheWork() {
        // Act
        Throwable thrown = catchThrowable(() -> runner.inTransaction(() -> {
            throw new IllegalStateException("save failed");
        }));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("save failed");
    }
}
