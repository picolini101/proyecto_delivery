package org.jala.edu.catalog.application.port.in;

/**
 * @param onlyActive {@code true} para listar solo comercios Activos (vista del cliente);
 *                   {@code false} para todos (administración y registro de productos)
 */
public record ListCommercesQuery(boolean onlyActive) {

    public static ListCommercesQuery all() {
        return new ListCommercesQuery(false);
    }

    public static ListCommercesQuery active() {
        return new ListCommercesQuery(true);
    }
}