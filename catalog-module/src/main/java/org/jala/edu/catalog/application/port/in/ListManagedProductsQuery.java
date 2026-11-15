package org.jala.edu.catalog.application.port.in;

/**
 * Entrada del caso de uso «listar productos para gestión». Vacía por ahora: lista todo el catálogo.
 * Los filtros (por comercio, categoría, etc.) se agregan aquí cuando se necesiten.
 */
public record ListManagedProductsQuery(String searchText) {
    /// Consulta con barra de busqeuda sin nada :lista todo el catálogo
    public ListManagedProductsQuery() {
        this(null);
    }
}