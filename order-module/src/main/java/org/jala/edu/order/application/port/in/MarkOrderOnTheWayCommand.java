package org.jala.edu.order.application.port.in;

import java.util.UUID;

public record MarkOrderOnTheWayCommand(
        UUID orderId,
        UUID changedById
) {}