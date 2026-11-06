package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;

/**
 * Usuario activo de la sesión para las pantallas del catálogo.
 * Centralizado aquí para no repetir la constante temporal en cada controlador.
 */
final class CatalogSession {

    // TODO(sprint 2): reemplazar por el usuario autenticado una vez que exista el inicio de sesión.
    static final UUID CURRENT_USER = SampleUsers.MERCHANT;

    private CatalogSession() {
    }
}