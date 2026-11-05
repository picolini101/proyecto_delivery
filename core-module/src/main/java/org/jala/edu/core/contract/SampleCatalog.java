package org.jala.edu.core.contract;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Fixed catalog data (#87), like {@link SampleUsers}: one commerce of {@link SampleUsers#MERCHANT} with an available
 * product and a sold-out one, so orders, deliveries and payments keep testing with the same ids before and after
 * {@code catalog-module} implements {@link CatalogService}. {@code catalog-module} stores them at startup once it
 * implements the contract; the fallback answers for them meanwhile.
 */
public final class SampleCatalog {

    /** Active commerce owned by {@link SampleUsers#MERCHANT}. */
    public static final UUID COMMERCE = UUID.fromString("00000000-0000-0000-0000-000000000301");
    public static final String COMMERCE_NAME = "Sample Pizzeria";

    /** Available product of {@link #COMMERCE}. */
    public static final UUID PIZZA = UUID.fromString("00000000-0000-0000-0000-000000000101");
    public static final String PIZZA_NAME = "Pizza";
    public static final BigDecimal PIZZA_PRICE = new BigDecimal("50.00");
    public static final int PIZZA_STOCK = 10;

    /** Sold-out product of {@link #COMMERCE}: stock 0. */
    public static final UUID EMPANADA = UUID.fromString("00000000-0000-0000-0000-000000000102");
    public static final String EMPANADA_NAME = "Empanada";
    public static final BigDecimal EMPANADA_PRICE = new BigDecimal("8.50");

    private SampleCatalog() {
    }
}
