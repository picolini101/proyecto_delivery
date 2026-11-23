package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

public record DeleteCommerceCommand(UUID requesterId, UUID commerceId) {
}