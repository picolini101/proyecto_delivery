package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.junit.jupiter.api.Test;

class CommerceFilterOptionsTest {

    private final CommerceSummary pizzeria = commerce("Pizzeria", CommerceStatus.ACTIVE);
    private final CommerceSummary cafe = commerce("Cafe", CommerceStatus.DISABLED);

    @Test
    void putsAllCommercesFirstAndThenEveryRegisteredCommerce() {
        var options = CommerceFilterOptions.options(List.of(pizzeria, cafe));

        assertThat(options).containsExactly(CommerceFilterOptions.ALL, pizzeria, cafe);
    }

    @Test
    void listsOnlyAllCommercesWhenThereAreNoCommerces() {
        assertThat(CommerceFilterOptions.options(List.of()))
                .containsExactly(CommerceFilterOptions.ALL);
    }

    @Test
    void allCommercesOptionHasNoIdSoItDoesNotFilter() {
        assertThat(CommerceFilterOptions.idOf(CommerceFilterOptions.ALL)).isNull();
        assertThat(CommerceFilterOptions.idOf(null)).isNull();
        assertThat(CommerceFilterOptions.idOf(pizzeria)).isEqualTo(pizzeria.id());
    }

    @Test
    void keepsTheRequestedCommerceWhileItStillExists() {
        assertThat(CommerceFilterOptions.resolveId(List.of(pizzeria, cafe), cafe.id()))
                .isEqualTo(cafe.id());
    }

    @Test
    void fallsBackToAllCommercesWhenTheRequestedCommerceWasDeleted() {
        assertThat(CommerceFilterOptions.resolveId(List.of(pizzeria), cafe.id())).isNull();
    }

    @Test
    void staysOnAllCommercesWhenNoCommerceWasRequested() {
        assertThat(CommerceFilterOptions.resolveId(List.of(pizzeria), null)).isNull();
    }

    @Test
    void findsTheOptionThatMatchesAnId() {
        var options = CommerceFilterOptions.options(List.of(pizzeria, cafe));

        assertThat(CommerceFilterOptions.optionFor(options, cafe.id())).isEqualTo(cafe);
        assertThat(CommerceFilterOptions.optionFor(options, null))
                .isEqualTo(CommerceFilterOptions.ALL);
    }

    private static CommerceSummary commerce(String name, CommerceStatus status) {
        return new CommerceSummary(
                UUID.randomUUID(), name, "Calle 1 # 2-3", "+573001234567", status, 0L);
    }
}