package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

public record EnableCommerceCommand(UUID requesterId, UUID commerceId) {
}