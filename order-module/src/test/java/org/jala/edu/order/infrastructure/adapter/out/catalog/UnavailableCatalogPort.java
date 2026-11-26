package org.jala.edu.order.infrastructure.adapter.out.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.order.application.port.out.CatalogPort;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;

/**
 * Catalogo de prueba que siempre esta caido: todas las consultas lanzan {@link CatalogUnavailableException}.
 * Sirve para probar que los casos de uso no rompen el flujo ni modifican el carrito cuando el catalogo falla.
 */
public class UnavailableCatalogPort implements CatalogPort {

    @Override
    public Optional<ProductInfo> findProduct(UUID productId) {
        throw new CatalogUnavailableException();
    }

    @Override
    public boolean isAvailable(UUID productId, int quantity) {
        throw new CatalogUnavailableException();
    }

    @Override
    public List<ProductInfo> findAvailableProducts() {
        throw new CatalogUnavailableException();
    }
}