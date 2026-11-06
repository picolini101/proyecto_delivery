package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

public record RegisterCommerceCommand(
        UUID requesterId,
        String name) {
}
