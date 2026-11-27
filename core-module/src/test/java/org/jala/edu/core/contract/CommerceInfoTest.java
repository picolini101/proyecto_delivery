package org.jala.edu.core.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommerceInfoTest {

    @Test
    void exposesTheIdAndTheName() {
        // Arrange
        UUID commerceId = UUID.randomUUID();

        // Act
        CommerceInfo commerce = new CommerceInfo(commerceId, "Pizzeria Napoli");

        // Assert
        assertThat(commerce.commerceId()).isEqualTo(commerceId);
        assertThat(commerce.name()).isEqualTo("Pizzeria Napoli");
    }

    @Test
    void requiresTheIdAndTheName() {
        // Act
        Throwable withoutId = catchThrowable(() -> new CommerceInfo(null, "Pizzeria Napoli"));
        Throwable withoutName = catchThrowable(() -> new CommerceInfo(UUID.randomUUID(), null));

        // Assert
        assertThat(withoutId).isInstanceOf(NullPointerException.class);
        assertThat(withoutName).isInstanceOf(NullPointerException.class);
    }
}
