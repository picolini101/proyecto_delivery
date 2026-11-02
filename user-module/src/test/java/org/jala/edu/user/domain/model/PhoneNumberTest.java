package org.jala.edu.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PhoneNumberTest {

    @ParameterizedTest
    @ValueSource(strings = {"7000000", "70000000", "+59170000000", "123456789012345", " 70000000 "})
    void acceptsSevenToFifteenDigitsWithAnOptionalPlus(String value) {
        // Arrange (value)

        // Act
        PhoneNumber phone = new PhoneNumber(value);

        // Assert
        assertThat(phone.value()).isEqualTo(value.strip());
    }

    @ParameterizedTest
    @ValueSource(strings = {"55-12", "700000a0", "123456", "1234567890123456", "591+70000000", "+"})
    void rejectsLettersSymbolsAndWrongLengths(String value) {
        // Arrange (value)

        // Act
        Throwable thrown = catchThrowable(() -> new PhoneNumber(value));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessageContaining("Invalid phone number");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void aMissingPhoneIsAllowed(String value) {
        // Arrange (value)

        // Act
        PhoneNumber phone = PhoneNumber.ofNullable(value);

        // Assert
        assertThat(phone).isNull();
    }
}
