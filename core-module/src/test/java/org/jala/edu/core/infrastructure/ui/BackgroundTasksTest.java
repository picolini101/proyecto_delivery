package org.jala.edu.core.infrastructure.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import org.jala.edu.core.contract.NotAuthenticatedException;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class BackgroundTasksTest {

    @Test
    void aMissingSessionEndsItEvenWhenWrapped() {
        // Arrange
        Throwable direct = new NotAuthenticatedException();
        Throwable wrapped = new ExecutionException(new IllegalStateException(new NotAuthenticatedException()));

        // Act + Assert
        assertThat(BackgroundTasks.endsSession(direct)).isTrue();
        assertThat(BackgroundTasks.endsSession(wrapped)).isTrue();
    }

    @Test
    void otherFailuresKeepTheSession() {
        // Arrange
        Throwable businessRule = new DomainException("Email already registered");

        // Act + Assert
        assertThat(BackgroundTasks.endsSession(businessRule)).isFalse();
        assertThat(BackgroundTasks.endsSession(null)).isFalse();
    }
}
