package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * Datos para registrar un comercio. {@code merchantId} es el MERCHANT dueño del comercio,
 * elegido por el ADMIN de la lista de comerciantes activos.
 */
public record RegisterCommerceCommand(
        UUID requesterId,
        String name,
        String address,
        String phone,
        UUID merchantId) {
}