package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Error de negocio: el servicio del catalogo no responde o fallo, asi que no se puede confirmar el producto,
 * su precio ni su stock. Quien la recibe debe detener la operacion SIN modificar el carrito.
 * Es una {@link DomainException} del core, asi que la pantalla muestra su mensaje tal cual
 * (ver {@code ErrorMessages.of}).
 */
public class CatalogUnavailableException extends DomainException {

    /** Mensaje para el cliente; nunca incluye detalles tecnicos. */
    public static final String MESSAGE = "El catalogo no esta disponible. Intenta de nuevo mas tarde.";

    /** Cuando el servicio respondio algo inutilizable (por ejemplo, null). */
    public CatalogUnavailableException() {
        super(MESSAGE);
    }

    /**
     * Cuando el servicio lanzo una excepcion. Se conserva como causa para poder depurar.
     *
     * @param cause la falla original del servicio
     */
    public CatalogUnavailableException(Throwable cause) {
        super(MESSAGE);
        initCause(cause);
    }
}