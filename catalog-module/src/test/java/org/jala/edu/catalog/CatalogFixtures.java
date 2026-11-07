package org.jala.edu.catalog;

import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.core.contract.SampleUsers;

/** Comercios de prueba con datos válidos, compartidos por las pruebas (DRY). */
public final class CatalogFixtures {

    public static final String ADDRESS = "Calle 1 # 2-3";
    public static final String PHONE = "+573001234567";

    private CatalogFixtures() {
    }

    public static Commerce disabledCommerce(String name) {
        return Commerce.register(name, ADDRESS, PHONE, SampleUsers.MERCHANT);
    }

    public static Commerce activeCommerce(String name) {
        Commerce commerce = disabledCommerce(name);
        commerce.activate();
        return commerce;
    }
}