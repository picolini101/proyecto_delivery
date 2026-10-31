package org.jala.edu.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    private static final UUID COMMERCE_ID = UUID.randomUUID();

    @Test
    void registersAProductInItsCommerceWithTrimmedText() {
        // Act
        Product product = Product.register(COMMERCE_ID, "  Pizza ", " Food  ", new BigDecimal("50.00"));

        // Assert
        assertThat(product.getId()).isNotNull();
        assertThat(product.getCommerceId()).isEqualTo(COMMERCE_ID);
        assertThat(product.getName()).isEqualTo("Pizza");
        assertThat(product.getCategory()).isEqualTo("Food");
        assertThat(product.getPrice()).isEqualByComparingTo("50.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01"})
    void rejectsAPriceThatIsNotGreaterThanZero(String price) {
        // Act
        Throwable thrown = catchThrowable(
                () -> Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal(price)));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Price must be greater than zero");
    }

    @Test
    void rejectsAMissingPrice() {
        // Act
        Throwable thrown = catchThrowable(() -> Product.register(COMMERCE_ID, "Pizza", "Food", null));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Price must be greater than zero");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsABlankName(String name) {
        // Act
        Throwable thrown = catchThrowable(
                () -> Product.register(COMMERCE_ID, name, "Food", new BigDecimal("10")));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Name cannot be blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsABlankCategory(String category) {
        // Act
        Throwable thrown = catchThrowable(
                () -> Product.register(COMMERCE_ID, "Pizza", category, new BigDecimal("10")));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Category cannot be blank");
    }
}