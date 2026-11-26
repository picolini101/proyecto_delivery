package org.jala.edu.order.domain.exception;

import java.util.List;
import java.util.UUID;

import org.jala.edu.core.domain.exception.ConflictException;

/**
 * Error de negocio: el precio de uno o mas productos del carrito cambio en el catalogo y el cliente todavia no
 * acepto el precio nuevo. La finalizacion de compra se detiene hasta que el cliente lo confirme. El carrito no cambia.
 * Es una {@link ConflictException} del core, asi que la pantalla muestra su mensaje tal cual
 * (ver {@code ErrorMessages.of}).
 */
public class PriceChangedException extends ConflictException {

    /** Ids de los productos cuyo precio cambio. */
    private final List<UUID> productIds;

    /**
     * @param message    texto para el usuario, que dice cuales precios cambiaron
     * @param productIds ids de los productos con precio nuevo
     */
    public PriceChangedException(String message, List<UUID> productIds) {
        super(message);
        this.productIds = List.copyOf(productIds);
    }

    /** @return ids de los productos cuyo precio cambio (lista de solo lectura) */
    public List<UUID> getProductIds() {
        return productIds;
    }
}