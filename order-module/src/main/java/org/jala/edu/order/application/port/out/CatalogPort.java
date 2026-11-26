package org.jala.edu.order.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;

/**
 * Puerto de salida (INT-001): define QUE necesita el modulo de pedidos del catalogo,
 * sin decir COMO se consigue. Los casos de uso solo conocen esta interfaz.
 * <p>
 * La clase que la implementa (un adaptador, por ejemplo {@code CatalogGatewayAdapter}) es la unica que conoce
 * el contrato {@code CatalogService} del core. Si ese contrato cambia, solo cambia el adaptador.
 * <p>
 * Todas las operaciones son de solo lectura y lanzan {@link CatalogUnavailableException} si el servicio falla.
 */
public interface CatalogPort {

    /**
     * @param productId id del producto
     * @return el producto con su comercio, nombre y precio vigente; vacio si el catalogo no lo conoce
     * @throws CatalogUnavailableException si el servicio falla
     */
    Optional<ProductInfo> findProduct(UUID productId);

    /**
     * @param productId id del producto
     * @param quantity  unidades que se quieren
     * @return true si el producto esta disponible y el stock cubre la cantidad
     * @throws CatalogUnavailableException si el servicio falla
     */
    boolean isAvailable(UUID productId, int quantity);

    /**
     * @return los productos que un cliente puede agregar al carrito; vacio si no hay ninguno
     * @throws CatalogUnavailableException si el servicio falla
     */
    List<ProductInfo> findAvailableProducts();
}