package org.jala.edu.delivery.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CourierTest {

    @Test
    void registersCourierWithValidData() {
        // Arrange
        String name = "Maria Perez";
        String phone = "3001234567";

        // Act
        Courier courier = Courier.register(name, phone, TransportMode.MOTORCYCLE);

        // Assert
        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo(name);
        assertThat(courier.getPhoneNumber()).isEqualTo(phone);
        assertThat(courier.getTransportMode()).isEqualTo(TransportMode.MOTORCYCLE);
        assertThat(courier.getRole()).isEqualTo(UserRole.COURIER);
        assertThat(courier.getCreatedAt()).isNotNull();
        assertThat(courier.getUpdatedAt()).isNotNull();
    }

    @Test
    void removesExtraSpacesFromNameAndPhone() {
        // Act
        Courier courier = Courier.register(
                "  Maria Perez  ",
                "  3001234567  ",
                TransportMode.BICYCLE
        );

        // Assert
        assertThat(courier.getName()).isEqualTo("Maria Perez");
        assertThat(courier.getPhoneNumber()).isEqualTo("3001234567");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void rejectsAnEmptyOrMissingName(String name) {
        // Act
        Throwable thrown = catchThrowable(
                () -> Courier.register(name, "3001234567", TransportMode.BICYCLE)
        );

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123456", "1234567890123456", "1234abc", "123 4567"})
    void rejectsAnInvalidPhoneNumber(String phone) {
        // Act
        Throwable thrown = catchThrowable(
                () -> Courier.register("Maria", phone, TransportMode.BICYCLE)
        );

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("El teléfono debe contener entre 7 y 15 dígitos");
    }

    @Test
    void rejectsAMissingTransportMode() {
        // Act
        Throwable thrown = catchThrowable(
                () -> Courier.register("Maria", "3001234567", null)
        );

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Selecciona un medio de transporte");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "+1234567", "3001234567", "123456789012345"})
    void acceptsValidPhoneNumbers(String phone) {
        assertThat(Courier.isValidPhoneNumber(phone)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123456", "1234567890123456", "1234abc"})
    void identifiesInvalidPhoneNumbers(String phone) {
        assertThat(Courier.isValidPhoneNumber(phone)).isFalse();
    }
}