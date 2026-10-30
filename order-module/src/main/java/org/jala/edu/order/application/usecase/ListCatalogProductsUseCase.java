package org.jala.edu.order.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;

/**
 * Caso de uso: obtener los productos que se ofrecen al armar un carrito.
 * <p>
 * Solucion temporal: el contrato {@link CatalogService} todavia no tiene un metodo para listar productos,
 * asi que mientras tanto se consultan por id los productos conocidos.
 * Cuando el catalogo publique el listado, solo hay que cambiar este caso de uso.
 */
public class ListCatalogProductsUseCase {

    /** Ids de los productos de prueba que se van a mostrar (uno disponible y uno agotado). */
    private static final List<UUID> KNOWN_PRODUCTS = List.of(
            FallbackCatalogService.AVAILABLE_PRODUCT,
            FallbackCatalogService.SOLD_OUT_PRODUCT);

    /** Para consultar cada producto por su id. */
    private final CatalogService catalog;

    /**
     * @param catalog servicio del catalogo
     */
    public ListCatalogProductsUseCase(CatalogService catalog) {
        this.catalog = catalog;
    }

    /**
     * Busca en el catalogo cada producto conocido y devuelve los que existen.
     *
     * @return lista de productos con nombre y precio
     */
    public List<ProductInfo> execute() {
        List<ProductInfo> products = new ArrayList<>();
        for (UUID productId : KNOWN_PRODUCTS) {
            // orElse(null): saca el producto del Optional, o null si no existe
            ProductInfo product = catalog.findProduct(productId).orElse(null);
            if (product != null) {
                products.add(product);
            }
        }
        return products;
    }
}