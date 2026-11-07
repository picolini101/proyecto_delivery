package org.jala.edu.catalog.application.dto;

import java.util.UUID;
import org.jala.edu.catalog.domain.model.CommerceStatus;

/** Vista de un comercio para la UI: evita exponer la entidad de dominio. */
public record CommerceSummary(
        UUID id,
        String name,
        String address,
        String phone,
        CommerceStatus status) {
}