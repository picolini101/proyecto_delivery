package org.jala.edu.delivery.domain.model;

import java.util.Comparator;

/**
 * The ways a courier can sort the delivery history. Each value is a sorting strategy: it carries the comparator that
 * puts the deliveries in that order, so a new order only needs a new value here.
 */
public enum DeliverySortOrder {

    MOST_RECENT(Comparator.comparing(CompletedDelivery::deliveredOn).reversed()),
    OLDEST_FIRST(Comparator.comparing(CompletedDelivery::deliveredOn)),
    HIGHEST_AMOUNT(Comparator.comparing(CompletedDelivery::amount).reversed());

    private final Comparator<CompletedDelivery> comparator;

    DeliverySortOrder(Comparator<CompletedDelivery> comparator) {
        this.comparator = comparator;
    }

    public Comparator<CompletedDelivery> comparator() {
        return comparator;
    }
}
