package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Regla de permisos del catálogo (INT-002): solo un ADMIN o MERCHANT activo puede modificarlo.
 * Compartida por los casos de uso que escriben en el catálogo, para no repetir la regla (DRY).
 */
class CatalogAccessPolicy {

    private final UserService userService;

    CatalogAccessPolicy(UserService userService) {
        this.userService = userService;
    }

    /**
     * @throws DomainException si el usuario no existe, no está activo o no tiene rol ADMIN o MERCHANT
     */
    void requireAuthorized(UUID requesterId) {
        if (!userService.isActiveWithRole(requesterId, UserRole.ADMIN, UserRole.MERCHANT)) {
            throw new DomainException("User not authorized for this operation");
        }
    }
}