package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Reglas de permisos del catálogo (INT-002), validadas con {@link UserService}.
 * Compartidas por los casos de uso para no repetirlas (DRY).
 */
class CatalogAccessPolicy {

    private static final String NOT_AUTHORIZED = "User not authorized for this operation";

    private final UserService userService;

    CatalogAccessPolicy(UserService userService) {
        this.userService = userService;
    }

    /** Operaciones sobre productos: ADMIN o MERCHANT activo. */
    void requireAuthorized(UUID requesterId) {
        require(requesterId, UserRole.ADMIN, UserRole.MERCHANT);
    }

    /** Operaciones sobre comercios (registrar y habilitar): solo ADMIN activo. */
    void requireAdmin(UUID requesterId) {
        require(requesterId, UserRole.ADMIN);
    }

    /** El dueño de un comercio debe ser un usuario activo con rol MERCHANT. */
    void requireActiveMerchant(UUID merchantId) {
        if (merchantId == null) {
            throw new DomainException("Commerce merchant is required");
        }
        if (!userService.isActiveWithRole(merchantId, UserRole.MERCHANT)) {
            throw new DomainException("The owner must be an active merchant");
        }
    }

    private void require(UUID requesterId, UserRole... roles) {
        if (!userService.isActiveWithRole(requesterId, roles)) {
            throw new DomainException(NOT_AUTHORIZED);
        }
    }
}