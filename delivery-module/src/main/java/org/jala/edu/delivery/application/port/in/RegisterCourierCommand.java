package org.jala.edu.delivery.application.port.in;

import java.util.UUID;
import org.jala.edu.delivery.domain.model.TransportMode;

/**
 * Data required to register a delivery driver
 */
public record RegisterCourierCommand(
        UUID adminId,
        String name,
        String phoneNumber,
        TransportMode transportMode) {
}