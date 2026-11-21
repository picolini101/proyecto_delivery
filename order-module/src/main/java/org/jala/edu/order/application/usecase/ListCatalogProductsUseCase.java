package org.jala.edu.order.application.usecase;

import java.util.List;

import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.order.application.port.out.CatalogPort;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;

/**
 * Caso de uso: obtener los productos que se ofrecen al armar un carrito.
 * Pide al catalogo real los productos disponibles (con stock y de un comercio activo); no usa listas fijas.
 */
public class ListCatalogProductsUseCase {

    /** Para consultar los productos disponibles. */
    private final CatalogPort catalog;

    /**
     * @param catalog puerto del catalogo
     */
    public ListCatalogProductsUseCase(CatalogPort catalog) {
        this.catalog = catalog;
    }

    /**
     * Devuelve los productos disponibles del catalogo, con su nombre y precio vigente.
     *
     * @return lista de productos; vacia si no hay ninguno
     * @throws CatalogUnavailableException si el catalogo no responde
     */
    public List<ProductInfo> execute() {
        return catalog.findAvailableProducts();
    }
}