package org.jala.edu.payment.domain.port;

import java.util.UUID;

// Puerto de salida para la validación de usuarios y roles (INT-002 / D-07).
public interface UserService {
    // Valida si un usuario existe, se encuentra en estado ACTIVO y posee el rol especificado.
    boolean isUserActiveAndHasRole(UUID userId, String role);
}