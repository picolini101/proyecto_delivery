package org.jala.edu.catalog.application.usecase;

import java.util.UUID;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Regla de activación de un comercio: necesita al menos un producto Disponible
 */
class CommerceActivationPolicy {

    static final String NO_AVAILABLE_PRODUCT_MESSAGE =
            "No se puede habilitar el comercio se necesita al menos un producto disponible";

    private final ProductRepositoryPort productRepository;

    CommerceActivationPolicy(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    /** Indica si el comercio tiene al menos un producto Disponible con stock (FUN-004 / FUN-005). */
    boolean hasAvailableProduct(UUID commerceId) {
        return productRepository.findByCommerceId(commerceId)
                .stream()
                .anyMatch(Product::isPurchasable);
    }

    void requireAvailableProduct(UUID commerceId) {
        if (!hasAvailableProduct(commerceId)) {
            throw new DomainException(NO_AVAILABLE_PRODUCT_MESSAGE);
        }
    }
}