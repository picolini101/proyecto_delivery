package org.jala.edu.order.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.jala.edu.order.infrastructure.adapter.out.catalog.CatalogGatewayAdapter;
import org.jala.edu.order.infrastructure.adapter.out.catalog.UnavailableCatalogPort;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link ListCatalogProductsUseCase}: los productos salen del catalogo, no de una lista fija.
 */
class ListCatalogProductsUseCaseTest {

    /** Devuelve lo que el catalogo ofrece como disponible (la pizza; la empanada esta agotada). */
    @Test
    void listsTheProductsTheCatalogOffers() {
        ListCatalogProductsUseCase useCase =
                new ListCatalogProductsUseCase(new CatalogGatewayAdapter(new FallbackCatalogService()));

        List<ProductInfo> products = useCase.execute();

        assertEquals(1, products.size());
        assertEquals(SampleCatalog.PIZZA, products.get(0).productId());
    }

    /** Si el catalogo esta caido, el error llega a la pantalla para informar al cliente. */
    @Test
    void reportsWhenTheCatalogIsUnavailable() {
        ListCatalogProductsUseCase useCase = new ListCatalogProductsUseCase(new UnavailableCatalogPort());

        assertThrows(CatalogUnavailableException.class, () -> useCase.execute());
    }
}