package org.jala.edu.catalog.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleCatalog;
import org.jala.edu.core.contract.SampleUsers;
import org.junit.jupiter.api.Test;

class SampleCommerceInitializerTest {

    private final InMemoryCommerceRepositoryAdapter commerces = new InMemoryCommerceRepositoryAdapter();
    private final InMemoryProductRepositoryAdapter products = new InMemoryProductRepositoryAdapter();
    private final SampleCommerceInitializer initializer = new SampleCommerceInitializer(commerces, products);

    @Test
    void loadsTheSampleCommerceOwnedByTheSampleMerchant() {
        initializer.afterPropertiesSet();

        Commerce commerce = commerces.findById(new CommerceId(SampleCatalog.COMMERCE)).orElseThrow();
        assertThat(commerce.getName()).isEqualTo(SampleCatalog.COMMERCE_NAME);
        assertThat(commerce.getMerchantId()).isEqualTo(SampleUsers.MERCHANT);
        assertThat(commerce.isActive()).isTrue();
    }

    @Test
    void loadsAnAvailablePizzaAndASoldOutEmpanada() {
        initializer.afterPropertiesSet();

        Product pizza = products.findById(new ProductId(SampleCatalog.PIZZA)).orElseThrow();
        assertThat(pizza.getStock()).isEqualTo(SampleCatalog.PIZZA_STOCK);
        assertThat(pizza.isPurchasable()).isTrue();

        Product empanada = products.findById(new ProductId(SampleCatalog.EMPANADA)).orElseThrow();
        assertThat(empanada.getStock()).isZero();
        assertThat(empanada.isPurchasable()).isFalse();
    }

    @Test
    void doesNotDuplicateTheDataWhenItRunsTwice() {
        initializer.afterPropertiesSet();
        initializer.afterPropertiesSet();

        assertThat(commerces.findAll()).hasSize(1);
        assertThat(products.findAll()).hasSize(19);
    }
}