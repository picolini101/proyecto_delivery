package org.jala.edu.delivery.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jala.edu.delivery.DeliveryFixtures.delivery;

import java.util.List;
import org.junit.jupiter.api.Test;

class DeliverySortOrderTest {

    private final CompletedDelivery centro = delivery("10.00", "2026-10-20");
    private final CompletedDelivery villaFatima = delivery("12.50", "2026-10-22");
    private final CompletedDelivery sopocachi = delivery("8.00", "2026-10-25");
    private final CompletedDelivery miraflores = delivery("15.75", "2026-10-28");
    private final List<CompletedDelivery> deliveries = List.of(villaFatima, miraflores, centro, sopocachi);

    @Test
    void mostRecentPutsTheLatestDeliveryFirst() {
        // Act
        List<CompletedDelivery> sorted = sort(DeliverySortOrder.MOST_RECENT);

        // Assert
        assertThat(sorted).containsExactly(miraflores, sopocachi, villaFatima, centro);
    }

    @Test
    void oldestFirstPutsTheEarliestDeliveryFirst() {
        // Act
        List<CompletedDelivery> sorted = sort(DeliverySortOrder.OLDEST_FIRST);

        // Assert
        assertThat(sorted).containsExactly(centro, villaFatima, sopocachi, miraflores);
    }

    @Test
    void highestAmountPutsTheBestPaidDeliveryFirst() {
        // Act
        List<CompletedDelivery> sorted = sort(DeliverySortOrder.HIGHEST_AMOUNT);

        // Assert
        assertThat(sorted).containsExactly(miraflores, villaFatima, centro, sopocachi);
    }

    private List<CompletedDelivery> sort(DeliverySortOrder order) {
        return deliveries.stream().sorted(order.comparator()).toList();
    }
}
