package org.jala.edu.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.catalog.domain.matching.ContainsIgnoreCaseStrategy;
import org.jala.edu.catalog.domain.matching.TextMatchStrategy;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

    private static final UUID COMMERCE_ID = UUID.randomUUID();
    private static final TextMatchStrategy STRATEGY = new ContainsIgnoreCaseStrategy();

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

    @Test
    void updatesTheEditableDataWithTrimmedText() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        product.update("  Pasta ", " Italian ", new BigDecimal("30.00"));

        // Assert
        assertThat(product.getName()).isEqualTo("Pasta");
        assertThat(product.getCategory()).isEqualTo("Italian");
        assertThat(product.getPrice()).isEqualByComparingTo("30.00");
        assertThat(product.getCommerceId()).isEqualTo(COMMERCE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void keepsTheOriginalDataWhenAnUpdateIsInvalid(String price) {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> product.update("Pasta", "Italian", new BigDecimal(price)));

        // Assert: el producto no quedó a medio modificar
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Price must be greater than zero");
        assertThat(product.getName()).isEqualTo("Pizza");
        assertThat(product.getCategory()).isEqualTo("Food");
        assertThat(product.getPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void registersAProductWithZeroStock() {
        // Act
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Assert
        assertThat(product.getStock()).isZero();
    }

    @Test
    void adjustsStockToAValidQuantity() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        product.adjustStock(20);

        // Assert
        assertThat(product.getStock()).isEqualTo(20);
    }

    @Test
    void rejectsNegativeStock() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> product.adjustStock(-1));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Stock cannot be negative");
        assertThat(product.getStock()).isZero();
    }

    @Test
    void reservesStockAndUpdatesTheAvailableQuantity() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(20);

        // Act
        product.reserveStock(5);

        // Assert
        assertThat(product.getStock()).isEqualTo(15);
    }

    @Test
    void rejectsReservationWhenStockIsInsufficient() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(5);

        // Act
        Throwable thrown = catchThrowable(() -> product.reserveStock(10));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Insufficient stock");
        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void releasesStockAndUpdatesTheAvailableQuantity() {
        // Arrange
        Product product = Product.register(COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);
        product.reserveStock(4);

        // Act
        product.releaseStock(4);

        // Assert
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void allowsAdjustingStockToZero() {
        Product product = Product.register(
                COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);

        product.adjustStock(0);

        assertThat(product.getStock()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsReservationWithNonPositiveQuantity(int quantity) {
        Product product = Product.register(
                COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);

        Throwable thrown = catchThrowable(() -> product.reserveStock(quantity));

        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");

        assertThat(product.getStock()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsReleaseWithNonPositiveQuantity(int quantity) {
        Product product = Product.register(
                COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(10);

        Throwable thrown = catchThrowable(() -> product.releaseStock(quantity));

        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Quantity must be greater than zero");

        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void rejectsReleaseWhenStockExceedsMaximumIntegerValue() {
        Product product = Product.register(
                COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(Integer.MAX_VALUE);

        Throwable thrown = catchThrowable(() -> product.releaseStock(1));

        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Stock exceeds the maximum allowed value");

        assertThat(product.getStock()).isEqualTo(Integer.MAX_VALUE);
    }
    @Test
    void isPurchasableOnlyWhenAvailableAndWithStock() {
        Product product = Product.register(
                COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        assertThat(product.isPurchasable()).isFalse(); // un producto nuevo nace con stock 0

        product.adjustStock(3);
        assertThat(product.isPurchasable()).isTrue();

        product.updateStatus(ProductStatus.OUT_OF_STOCK);
        assertThat(product.isPurchasable()).isFalse();
    }

    @Test
        void doesNotAllowReactivatingADisabledProduct() {
            Product product = Product.register(
                    COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));
            product.adjustStock(10);
            product.disable();

            Throwable thrown = catchThrowable(
                    () -> product.updateStatus(ProductStatus.AVAILABLE));

            assertThat(thrown)
                    .isInstanceOf(DomainException.class)
                    .hasMessage("Disabled product cannot be reactivated");

            assertThat(product.getStatus()).isEqualTo(ProductStatus.DISABLED);
            assertThat(product.getStock()).isEqualTo(10);
        }

    @ParameterizedTest
    @ValueSource(strings = {"pizza", "PIZZA", "izz", "marg", "food", "OO", "  pizza  "})
    void matchesPartiallyByNameOrCategoryIgnoringCase(String text) {
        Product product = Product.register(
                COMMERCE_ID, "Pizza Margarita", "Food", new BigDecimal("50.00"));

        assertThat(product.matches(text, STRATEGY)).isTrue();

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void matchesEverythingWhenTheTextIsBlank(String text) {
        Product product = Product.register(
                COMMERCE_ID, "Pizza Margarita", "Food", new BigDecimal("50.00"));

        assertThat(product.matches(text, STRATEGY)).isTrue();
    }

    @Test
    void doesNotMatchWhenTheTextIsInNeitherNameNorCategory() {
        Product product = Product.register(
                COMMERCE_ID, "Pizza Margarita", "Food", new BigDecimal("50.00"));

        assertThat(product.matches("burger", STRATEGY)).isFalse();
    }
}