package org.jala.edu.order.application.port.in;

import java.util.UUID;

public record MarkOrderCommand(
        UUID orderId,
        UUID changedById
) {}