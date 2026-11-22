package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.port.in.ChangeCommerceStatusCommand;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;

class ChangeCommerceStatusUseCaseTest {

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();

    private final InMemoryProductRepositoryAdapter productRepository =      // NUEVO
            new InMemoryProductRepositoryAdapter();

    private final ChangeCommerceStatusUseCase changeStatus =
            new ChangeCommerceStatusUseCase( repository, productRepository, new FallbackUserService());

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void disablesAnActiveCommerce(UUID requesterId) {
        Commerce commerce = repository.save(CatalogFixtures.activeCommerce("My Commerce"));

        changeStatus.execute(new ChangeCommerceStatusCommand(
                requesterId, commerce.getId().value(), CommerceStatus.DISABLED));

        assertThat(repository.findById(commerce.getId()).orElseThrow().getStatus())
                .isEqualTo(CommerceStatus.DISABLED);
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void activatesADisabledCommerce(UUID requesterId) {
        Commerce commerce = CatalogFixtures.disabledCommerce("My Commerce");
        repository.save(commerce);
        addProduct(commerce, 5);

        changeStatus.execute(new ChangeCommerceStatusCommand(
                requesterId, commerce.getId().value(), CommerceStatus.ACTIVE));

        assertThat(repository.findById(commerce.getId()).orElseThrow().getStatus())
                .isEqualTo(CommerceStatus.ACTIVE);
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsUnauthorizedUsers(UUID requesterId) {
        Commerce commerce = repository.save(CatalogFixtures.activeCommerce("My Commerce"));

        Throwable thrown = catchThrowable(() -> changeStatus.execute(
                new ChangeCommerceStatusCommand(
                        requesterId, commerce.getId().value(), CommerceStatus.DISABLED)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void rejectsAnUnknownCommerce() {
        Throwable thrown = catchThrowable(() -> changeStatus.execute(
                new ChangeCommerceStatusCommand(
                        SampleUsers.ADMIN, UUID.randomUUID(), CommerceStatus.DISABLED)));

        assertThat(thrown).isInstanceOf(CommerceNotFoundException.class);
    }

    @Test
    void rejectsANullStatus() {
        Commerce commerce = repository.save(CatalogFixtures.activeCommerce("My Commerce"));

        Throwable thrown = catchThrowable(() -> changeStatus.execute(
                new ChangeCommerceStatusCommand(
                        SampleUsers.ADMIN, commerce.getId().value(), null)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce status cannot be null");
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void rejectsActivationWithoutProducts() {
        Commerce commerce = repository.save(CatalogFixtures.disabledCommerce("Empty"));

        assertActivationRejected(commerce);
    }

    @Test
    void rejectsActivationWhenAllProductsAreOutOfStock() {
        Commerce commerce = repository.save(CatalogFixtures.disabledCommerce("Sold out"));
        addProduct(commerce, 0); // stock 0
        addProduct(commerce, 5).updateStatus(ProductStatus.OUT_OF_STOCK); // estado Agotado

        assertActivationRejected(commerce);
    }

    @Test
    void rejectsActivationWhenAllProductsAreDisabled() {
        Commerce commerce = repository.save(CatalogFixtures.disabledCommerce("Disabled products"));
        addProduct(commerce, 5).disable();

        assertActivationRejected(commerce);
    }

    @Test
    void activatesWhenAtLeastOneProductIsAvailable() {
        Commerce commerce = repository.save(CatalogFixtures.disabledCommerce("Mixed"));
        addProduct(commerce, 0);
        addProduct(commerce, 3);

        changeStatus.execute(new ChangeCommerceStatusCommand(
                SampleUsers.ADMIN, commerce.getId().value(), CommerceStatus.ACTIVE));

        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void disablesAnEmptyCommerceWithoutRestriction() {
        Commerce commerce = repository.save(CatalogFixtures.activeCommerce("Empty active"));

        changeStatus.execute(new ChangeCommerceStatusCommand(
                SampleUsers.ADMIN, commerce.getId().value(), CommerceStatus.DISABLED));

        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void ignoresAvailableProductsOfOtherCommerces() {
        Commerce commerce = repository.save(CatalogFixtures.disabledCommerce("Mine"));
        Commerce other = repository.save(CatalogFixtures.activeCommerce("Other"));
        addProduct(other, 5);

        assertActivationRejected(commerce);
    }

    private void assertActivationRejected(Commerce commerce) {
        Throwable thrown = catchThrowable(() -> changeStatus.execute(
                new ChangeCommerceStatusCommand(
                        SampleUsers.ADMIN, commerce.getId().value(), CommerceStatus.ACTIVE)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage(CommerceActivationPolicy.NO_AVAILABLE_PRODUCT_MESSAGE);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    private Product addProduct(Commerce owner, int stock) {
        Product product = Product.register(
                owner.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(stock);
        return productRepository.save(product);
    }
}