package org.jala.edu.catalog.infrastructure.adapter.in.ui;

/**
 * Ubicaciones en el classpath de las vistas del módulo de catálogo.
 */
public final class CatalogViews {

    private static final String BASE = "/org/jala/edu/catalog/infrastructure/adapter/in/ui/";

    /** Vista principal del módulo, abierta mediante el botón de navegación «Catálogo». */
    public static final String MAIN = BASE + "catalog-main.fxml";
    public static final String CREATE_PRODUCT = BASE + "create-product.fxml";

    private CatalogViews() {
    }
}