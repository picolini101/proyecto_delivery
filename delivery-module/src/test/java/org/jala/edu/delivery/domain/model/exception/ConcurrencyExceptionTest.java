package org.jala.edu.delivery.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class ConcurrencyExceptionTest {

    @Test
    void hasTheExpectedErrorMessage() {
        // Arrange
        ConcurrencyException exception =
                new ConcurrencyException(new IllegalStateException());

        // Assert
        assertThat(exception)
                .isInstanceOf(DomainException.class)
                .hasMessage(ConcurrencyException.MESSAGE);
    }

    @Test
    void keepsTheOriginalCause() {
        // Arrange
        Throwable cause = new IllegalStateException("El pedido ya fue asignado");

        // Act
        ConcurrencyException exception = new ConcurrencyException(cause);

        // Assert
        assertThat(exception.getCause()).isSameAs(cause);
    }
}