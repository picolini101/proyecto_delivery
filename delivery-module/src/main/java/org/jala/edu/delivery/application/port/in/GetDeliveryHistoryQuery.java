package org.jala.edu.delivery.application.port.in;

import java.util.UUID;
import org.jala.edu.delivery.domain.model.DeliverySortOrder;

/**
 * Input of the "get delivery history" use case: the courier whose finished deliveries are requested and the order to
 * show them in. Without an order, the most recent deliveries come first.
 */
public record GetDeliveryHistoryQuery(UUID courierId, DeliverySortOrder sortOrder) {

    public GetDeliveryHistoryQuery {
        if (sortOrder == null) {
            sortOrder = DeliverySortOrder.MOST_RECENT;
        }
    }

    public GetDeliveryHistoryQuery(UUID courierId) {
        this(courierId, DeliverySortOrder.MOST_RECENT);
    }
}
