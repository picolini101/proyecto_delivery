package org.jala.edu.catalog.infrastructure.adapter.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jala.edu.catalog.application.port.out.ProductRepositoryPort;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.CommerceInfo;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;
import org.jala.edu.catalog.application.port.out.CommerceRepositoryPort;
import org.jala.edu.catalog.domain.model.CommerceId;
import java.util.ArrayList;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.CatalogProductPage;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.usecase.ListAvailableProductsUseCase;

/**
 * Adaptador que expone las operaciones del catálogo al resto del sistema.
 */
@Component
@RequiredArgsConstructor
public class CatalogServiceAdapter implements CatalogService {

    private final ProductRepositoryPort productRepository;
    private final CommerceRepositoryPort commerceRepository;
    private final ListAvailableProductsUseCase listAvailableProducts;

    @Override
    public Optional<ProductInfo> findProduct(UUID productId) {
        return findDomainProduct(productId).map(this::toProductInfo);
    }

    @Override
    public Optional<CommerceInfo> findCommerce(UUID commerceId) {
        return commerceRepository.findById(new CommerceId(commerceId))
                .map(commerce -> new CommerceInfo(commerce.getId().value(), commerce.getName()));
    }

    @Override
    public boolean isAvailable(UUID productId, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        return findDomainProduct(productId)
                .filter(Product::isAvailable)
                .filter(product -> product.getStock() >= quantity)
                .filter(product -> commerceRepository.findById(
                                new CommerceId(product.getCommerceId()))
                        .map(commerce -> commerce.isActive())
                        .orElse(false))
                .isPresent();
    }

    @Override
    public void reserveStock(UUID productId, int quantity) {
        Product product = getRequiredProduct(productId);

        if (!product.isAvailable()) {
            throw new ConflictException("Product is not available");
        }

        if (quantity <= 0 || product.getStock() < quantity) {
            throw new ConflictException("Insufficient stock");
        }

        product.reserveStock(quantity);
        productRepository.save(product);
    }

    @Override
    public void releaseStock(UUID productId, int quantity) {
        Product product = getRequiredProduct(productId);

        if (quantity <= 0) {
            throw new ConflictException("Quantity must be greater than zero");
        }

        product.releaseStock(quantity);
        productRepository.save(product);
    }

    @Override
    public List<UUID> findCommerceIdsByMerchant(UUID merchantId) {
        if (merchantId == null) {
            return List.of();
        }

        return commerceRepository.findAll().stream()
                .filter(commerce -> commerce.getMerchantId().equals(merchantId))
                .map(commerce -> commerce.getId().value())
                .toList();
    }

    @Override
    public List<ProductInfo> findAvailableProducts() {
        return commerceRepository.findAll().stream()
                .flatMap(commerce -> availableProductsOf(commerce.getId().value()).stream())
                .toList();
    }

    private List<ProductInfo> availableProductsOf(UUID commerceId) {
        List<ProductInfo> result = new ArrayList<>();
        int page = 1;
        int totalPages;
        do {
            CatalogProductPage current =
                    listAvailableProducts.execute(new ListAvailableProductsQuery(commerceId, page));
            current.products().stream()
                    .filter(CatalogProduct::available)
                    .map(p -> new ProductInfo(p.id(), commerceId, p.name(), p.price()))
                    .forEach(result::add);
            totalPages = current.totalPages();
            page++;
        } while (page <= totalPages);
        return result;
    }

    private Optional<Product> findDomainProduct(UUID productId) {
        if (productId == null) {
            return Optional.empty();
        }

        return productRepository.findById(new ProductId(productId))
                .filter(product -> commerceRepository.existsById(
                        new CommerceId(product.getCommerceId())));
    }

    private Product getRequiredProduct(UUID productId) {
        if (productId == null) {
            throw new NotFoundException("Product not found");
        }

        return findDomainProduct(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }

    private ProductInfo toProductInfo(Product product) {
        return new ProductInfo(
                product.getId().value(),
                product.getCommerceId(),
                product.getName(),
                product.getPrice());
    }
}