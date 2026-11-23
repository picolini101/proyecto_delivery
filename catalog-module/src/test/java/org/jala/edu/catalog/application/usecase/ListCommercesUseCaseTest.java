
package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListCommercesUseCaseTest {

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();

    private final ListCommercesUseCase listCommerces =
            new ListCommercesUseCase(repository, productRepository);

    @Test
    void returnsNothingWhenThereAreNoCommerces() {
        assertThat(listCommerces.execute(ListCommercesQuery.all())).isEmpty();
    }

    @Test
    void listsEveryCommerceWithItsData() {
        repository.save(CatalogFixtures.disabledCommerce("Disabled one"));
        repository.save(CatalogFixtures.activeCommerce("Active one"));

        var commerces = listCommerces.execute(ListCommercesQuery.all());

        assertThat(commerces).extracting(CommerceSummary::name)
                .containsExactly("Disabled one", "Active one");
        assertThat(commerces).extracting(CommerceSummary::status)
                .containsExactly(CommerceStatus.DISABLED, CommerceStatus.ACTIVE);
        assertThat(commerces.get(0).address()).isEqualTo(CatalogFixtures.ADDRESS);
        assertThat(commerces.get(0).phone()).isEqualTo(CatalogFixtures.PHONE);
    }

    @Test
    void listsOnlyTheActiveCommercesWhenRequested() {
        repository.save(CatalogFixtures.disabledCommerce("Disabled one"));
        repository.save(CatalogFixtures.activeCommerce("Active one"));

        var commerces = listCommerces.execute(ListCommercesQuery.active());

        assertThat(commerces).extracting(CommerceSummary::name).containsExactly("Active one");
    }

    @Test
    void includesProductCountForEachCommerce() {
        var firstCommerce = repository.save(CatalogFixtures.activeCommerce("First"));
        var secondCommerce = repository.save(CatalogFixtures.activeCommerce("Second"));

        Product pizza = Product.register(
                firstCommerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        pizza.adjustStock(10);
        productRepository.save(pizza);

        Product soda = Product.register(
                firstCommerce.getId().value(), "Soda", "Drinks", new BigDecimal("5.00"));
        soda.adjustStock(0);
        productRepository.save(soda);

        var commerces = listCommerces.execute(ListCommercesQuery.all());

        assertThat(commerces)
                .extracting(CommerceSummary::productCount)
                .containsExactly(2L, 0L);
    }
}
