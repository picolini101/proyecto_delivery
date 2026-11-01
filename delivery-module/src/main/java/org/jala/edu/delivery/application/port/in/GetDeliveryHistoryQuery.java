package org.jala.edu.delivery.application.port.in;

import java.util.UUID;

/**
 * Input of the "get delivery history" use case: the courier whose finished deliveries are requested.
 */
public record GetDeliveryHistoryQuery(UUID courierId) {
}
