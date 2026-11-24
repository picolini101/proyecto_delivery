package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

/**
 * Entrada del caso de uso «listar productos para gestión».
 *
 * @param searchText texto para buscar por nombre o categoría; vacío o {@code null} no filtra
 * @param commerceId comercio por el que filtrar; {@code null} no filtra (todos los comercios)
 */
public record ListManagedProductsQuery(String searchText, UUID commerceId) {

    /** Consulta solo con texto de búsqueda: no filtra por comercio. */
    public ListManagedProductsQuery(String searchText) {
        this(searchText, null);
    }

    /** Consulta sin filtros: lista todo el catálogo. */
    public ListManagedProductsQuery() {
        this(null, null);
    }
}