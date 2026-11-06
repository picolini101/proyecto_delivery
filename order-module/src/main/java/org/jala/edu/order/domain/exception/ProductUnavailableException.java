package org.jala.edu.order.domain.exception;

import org.jala.edu.core.domain.exception.ConflictException;

import java.util.List;
import java.util.UUID;

/**
 * Error de negocio: uno o mas productos del carrito ya no existen en el catalogo o ya no hay stock
 * para la cantidad pedida. Quien la lanza (por ejemplo la finalizacion de compra) detiene la operacion.
 * Es una {@link ConflictException} del core, asi que la pantalla muestra su mensaje tal cual
 * (ver {@code ErrorMessages.of}).
 */
public class ProductUnavailableException extends ConflictException {

    /** Ids de los productos que cambiaron de disponibilidad. */
    private final List<UUID> productIds;

    /**
     * @param message    texto para el usuario, que dice cuales productos cambiaron
     * @param productIds ids de los productos con problemas
     */
    public ProductUnavailableException(String message, List<UUID> productIds) {
        super(message);
        this.productIds = List.copyOf(productIds);
    }

    /** @return ids de los productos que cambiaron de disponibilidad (basicamente una lista de solo lectura) */
    public List<UUID> getProductIds() {
        return productIds;
    }
}
