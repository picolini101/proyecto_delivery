package org.jala.edu.order.infrastructure.adapter.out.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.order.application.port.out.CatalogPort;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;

/**
 * Adaptador (patron Adapter) que conecta el puerto {@link CatalogPort} del modulo de pedidos con el
 * contrato {@link CatalogService} del core, que implementa el modulo de catalogo.
 * <p>
 * Su unica regla es proteger al modulo de pedidos de las fallas del catalogo: cualquier excepcion o respuesta
 * null del servicio se convierte en {@link CatalogUnavailableException}, para que el caso de uso nunca dependa
 * de como falla el otro modulo. No tiene reglas de negocio ni guarda estado, asi que siempre devuelve el
 * precio y el stock vigentes. Se registra como bean en {@code OrderModuleConfiguration}.
 */
public class CatalogGatewayAdapter implements CatalogPort {

    /** Contrato del core que atiende el catalogo. */
    private final CatalogService catalog;

    /**
     * Recibe el servicio del catalogo. Spring lo entrega desde {@code OrderModuleConfiguration}.
     *
     * @param catalog servicio del catalogo del core
     */
    public CatalogGatewayAdapter(CatalogService catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<ProductInfo> findProduct(UUID productId) {
        return call(() -> catalog.findProduct(productId));
    }

    @Override
    public boolean isAvailable(UUID productId, int quantity) {
        return call(() -> catalog.isAvailable(productId, quantity));
    }

    @Override
    public List<ProductInfo> findAvailableProducts() {
        return call(catalog::findAvailableProducts);
    }

    /**
     * Ejecuta la consulta y traduce las fallas.
     *
     * @param request consulta al catalogo
     * @param <T>     tipo de la respuesta
     * @return la respuesta del catalogo, nunca null
     * @throws CatalogUnavailableException si el servicio lanzo una excepcion o respondio null
     */
    private <T> T call(Supplier<T> request) {
        T response;
        try {
            response = request.get();
        } catch (RuntimeException e) {
            // El catalogo no respondio o devolvio un error: no sabemos precio ni stock, asi que no se avanza.
            throw new CatalogUnavailableException(e);
        }
        if (response == null) {
            throw new CatalogUnavailableException();
        }
        return response;
    }
}