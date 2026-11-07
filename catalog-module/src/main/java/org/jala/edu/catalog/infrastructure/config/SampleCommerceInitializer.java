package org.jala.edu.catalog.infrastructure.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Carga {@link SampleCatalog} al iniciar en modo memoria (#87): el comercio de prueba del MERCHANT de ejemplo
 * (Activo) con Pizza (disponible, stock 10) y Empanada (agotada), con los mismos ids que usan pedidos,
 * entregas y pagos. Solo guarda lo que falta, así que no duplica nada.
 */
@Component
@ConditionalOnInMemoryPersistence
public class SampleCommerceInitializer implements InitializingBean {

    private static final String ADDRESS = "Calle 1 # 2-3";
    private static final String PHONE = "+573001234567";
    private static final String CATEGORY = "Comida";

    private final CommerceRepositoryPort commerceRepository;
    private final ProductRepositoryPort productRepository;

    public SampleCommerceInitializer(
            CommerceRepositoryPort commerceRepository,
            ProductRepositoryPort productRepository) {
        this.commerceRepository = commerceRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void afterPropertiesSet() {
        Instant now = Instant.now();
        saveCommerceIfMissing(now);
        saveProductIfMissing(SampleCatalog.PIZZA, SampleCatalog.PIZZA_NAME,
                SampleCatalog.PIZZA_PRICE, SampleCatalog.PIZZA_STOCK, now);

        Product empanada = saveProductIfMissing(SampleCatalog.EMPANADA, SampleCatalog.EMPANADA_NAME,
                SampleCatalog.EMPANADA_PRICE, 0, now);
        if (empanada != null) {
            empanada.updateStatus(ProductStatus.OUT_OF_STOCK);
            productRepository.save(empanada);
        }
    }

    private void saveCommerceIfMissing(Instant now) {
        CommerceId commerceId = new CommerceId(SampleCatalog.COMMERCE);
        if (commerceRepository.findById(commerceId).isPresent()) {
            return;
        }
        commerceRepository.save(Commerce.restore(
                commerceId,
                SampleCatalog.COMMERCE_NAME,
                ADDRESS,
                PHONE,
                SampleUsers.MERCHANT,
                CommerceStatus.ACTIVE,
                now,
                now));
    }

    /** @return el producto recién guardado, o {@code null} si ya existía */
    private Product saveProductIfMissing(UUID id, String name, BigDecimal price, int stock, Instant now) {
        ProductId productId = new ProductId(id);
        if (productRepository.findById(productId).isPresent()) {
            return null;
        }
        return productRepository.save(Product.restore(
                productId, SampleCatalog.COMMERCE, name, CATEGORY, price, stock, now, now));
    }
}