package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.CommerceSummary;
import org.jala.edu.catalog.application.port.in.ListCommercesQuery;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListCommercesUseCaseTest {

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();
    private final ListCommercesUseCase listCommerces = new ListCommercesUseCase(repository);

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
}