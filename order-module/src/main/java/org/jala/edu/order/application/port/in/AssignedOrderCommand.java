package org.jala.edu.order.application.port.in;

import java.util.UUID;

public record AssignedOrderCommand(
        UUID orderId,
        UUID courierId,
        UUID changedById) {}