package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.catalog.application.dto.CatalogProduct;
import org.jala.edu.catalog.application.dto.EnableCommerceResult;
import org.jala.edu.catalog.application.port.in.AdjustProductStockCommand;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.application.port.in.EnableCommerceCommand;
import org.jala.edu.catalog.application.port.in.ListAvailableProductsQuery;
import org.jala.edu.catalog.application.port.in.RegisterCommerceCommand;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.Test;

/**
 * Flujo completo con repositorios en memoria: registrar comercio, registrar un producto en él,
 * darle stock, habilitar el comercio y verlo en el catálogo del cliente.
 */
class CommerceEnablementFlowTest {

    @Test
    void anEnabledCommerceShowsItsAvailableProductsInTheCatalog() {
        var commerces = new InMemoryCommerceRepositoryAdapter();
        var products = new InMemoryProductRepositoryAdapter();
        var users = new FallbackUserService();

        UUID commerceId = new RegisterCommerceUseCase(commerces, users).execute(
                new RegisterCommerceCommand(
                        SampleUsers.ADMIN, "Pizzeria", "Calle 1 # 2-3", "+573001234567",
                        SampleUsers.MERCHANT));
        UUID productId = new CreateProductUseCase(products, commerces, users).execute(
                new CreateProductCommand(
                        SampleUsers.ADMIN, commerceId, "Pizza", "Food", new BigDecimal("50.00")));
        new AdjustProductStockUseCase(products, users).execute(
                new AdjustProductStockCommand(SampleUsers.ADMIN, productId, 10));
        var listCatalog = new ListAvailableProductsUseCase(products, commerces);

        // Antes de habilitar, el cliente no ve nada
        assertThat(listCatalog.execute(new ListAvailableProductsQuery(commerceId))).isEmpty();

        EnableCommerceResult result = new EnableCommerceUseCase(commerces, products, users)
                .execute(new EnableCommerceCommand(SampleUsers.ADMIN, commerceId));

        // Después de habilitar, aparece su producto disponible
        List<CatalogProduct> catalog = listCatalog.execute(new ListAvailableProductsQuery(commerceId));
        assertThat(result).isEqualTo(EnableCommerceResult.ENABLED);
        assertThat(catalog).extracting(CatalogProduct::name).containsExactly("Pizza");
    }
}