package org.jala.edu.catalog.application.port.in;

import java.util.UUID;
import org.jala.edu.catalog.domain.model.CommerceStatus;

public record ChangeCommerceStatusCommand(
        UUID requesterId,
        UUID commerceId,
        CommerceStatus status) {
}
