package org.jala.edu.core.infrastructure.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.core.contract.NotAuthenticatedException;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class ErrorMessagesTest {

    @Test
    void showsBusinessRulesAndMissingSessionsAsTheyAre() {
        // Arrange
        Throwable rule = new DomainException("Price must be greater than zero");
        Throwable noSession = new NotAuthenticatedException();

        // Act + Assert
        assertThat(ErrorMessages.of(rule, "save")).isEqualTo("Price must be greater than zero");
        assertThat(ErrorMessages.of(noSession, "save")).isEqualTo("No user has signed in");
    }

    @Test
    void hidesTheDetailsOfUnexpectedErrors() {
        // Arrange
        Throwable bug = new IllegalStateException("connection refused");

        // Act
        String message = ErrorMessages.of(bug, "save the product");

        // Assert
        assertThat(message).isEqualTo("Unexpected error: could not save the product. Please try again.");
    }
}
