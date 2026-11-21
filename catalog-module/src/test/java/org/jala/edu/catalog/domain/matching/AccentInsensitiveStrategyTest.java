package org.jala.edu.catalog.domain.matching;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccentInsensitiveStrategyTest {

    private final TextMatchStrategy strategy = new AccentInsensitiveStrategy();

    @ParameterizedTest
    @CsvSource({
            "Café,cafe",
            "Cafe,café",
            "CAFÉ,cafe",
            "Pizzería,PIZZERIA",
            "Crème brûlée,creme brulee"})
    void matchesIgnoringAccentsAndCase(String candidate, String query) {
        assertThat(strategy.matches(candidate, query)).isTrue();
    }

    @Test
    void doesNotMatchTextThatIsNotContained() {
        assertThat(strategy.matches("Pizza", "burger")).isFalse();
    }
}