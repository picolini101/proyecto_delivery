package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.junit.jupiter.api.Test;

class CatalogEmptyStateTest {

    @Test
    void returnsTrueWhenCatalogIsEmpty() {
        assertThat(CatalogEmptyState.hasNoAvailableProducts(List.of())).isTrue();
    }

    @Test
    void returnsTrueWhenAllProductsAreSoldOut() {
        CatalogProduct pizza = product("Pizza", false);
        CatalogProduct soda = product("Soda", false);

        assertThat(CatalogEmptyState.hasNoAvailableProducts(List.of(pizza, soda))).isTrue();
    }

    @Test
    void returnsFalseWhenAtLeastOneProductIsAvailable() {
        CatalogProduct pizza = product("Pizza", true);
        CatalogProduct soda = product("Soda", false);

        assertThat(CatalogEmptyState.hasNoAvailableProducts(List.of(pizza, soda))).isFalse();
    }

    private CatalogProduct product(String name, boolean available) {
        return new CatalogProduct(
                UUID.randomUUID(),
                name,
                "Food",
                new BigDecimal("50.00"),
                available
        );
    }
}