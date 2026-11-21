package org.jala.edu.catalog.domain.matching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContainsIgnoreCaseStrategyTest {

    private final TextMatchStrategy strategy = new ContainsIgnoreCaseStrategy();

    @ParameterizedTest
    @CsvSource({"Pizza Margarita,MARG", "Pizza,izz", "Food,food"})
    void matchesPartialTextIgnoringCase(String candidate, String query) {
        assertThat(strategy.matches(candidate, query)).isTrue();
    }

    @Test
    void doesNotIgnoreAccents() {
        assertThat(strategy.matches("Café", "cafe")).isFalse();
    }

    @Test
    void doesNotMatchTextThatIsNotContained() {
        assertThat(strategy.matches("Pizza", "burger")).isFalse();
    }
}