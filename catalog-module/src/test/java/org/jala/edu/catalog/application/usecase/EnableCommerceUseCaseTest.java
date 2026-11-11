package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.dto.EnableCommerceResult;
import org.jala.edu.catalog.application.port.in.EnableCommerceCommand;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EnableCommerceUseCaseTest {

    private static final String NO_PRODUCT_MESSAGE =
            "No se puede habilitar el comercio se necesita al menos un producto disponible";

    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();
    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final EnableCommerceUseCase enableCommerce = new EnableCommerceUseCase(
            commerceRepository, productRepository, new FallbackUserService());

    private Commerce commerce;

    static Stream<UUID> nonAdminUsers() {
        return Stream.of(SampleUsers.MERCHANT, SampleUsers.CUSTOMER, SampleUsers.COURIER,
                UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.disabledCommerce("Pizzeria"));
    }

    @Test
    void enablesADisabledCommerceThatHasAnAvailableProduct() {
        addProduct(commerce, 5);

        EnableCommerceResult result = enableCommerce.execute(command(SampleUsers.ADMIN));

        assertThat(result).isEqualTo(EnableCommerceResult.ENABLED);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @Test
    void rejectsACommerceWithoutProducts() {
        Throwable thrown = catchThrowable(() -> enableCommerce.execute(command(SampleUsers.ADMIN)));

        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage(NO_PRODUCT_MESSAGE);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void rejectsACommerceWhoseOnlyProductHasNoStock() {
        addProduct(commerce, 0);

        Throwable thrown = catchThrowable(() -> enableCommerce.execute(command(SampleUsers.ADMIN)));

        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage(NO_PRODUCT_MESSAGE);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void rejectsACommerceWhoseOnlyProductIsOutOfStock() {
        Product product = addProduct(commerce, 5);
        product.updateStatus(ProductStatus.OUT_OF_STOCK);

        Throwable thrown = catchThrowable(() -> enableCommerce.execute(command(SampleUsers.ADMIN)));

        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage(NO_PRODUCT_MESSAGE);
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void ignoresTheAvailableProductsOfOtherCommerces() {
        Commerce other = commerceRepository.save(CatalogFixtures.activeCommerce("Other"));
        addProduct(other, 5);

        Throwable thrown = catchThrowable(() -> enableCommerce.execute(command(SampleUsers.ADMIN)));

        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage(NO_PRODUCT_MESSAGE);
    }

    @Test
    void reportsAnAlreadyActiveCommerceAndChangesNothing() {
        Commerce active = commerceRepository.save(CatalogFixtures.activeCommerce("Active"));
        Instant updatedAtBefore = active.getUpdatedAt();

        EnableCommerceResult result = enableCommerce.execute(
                new EnableCommerceCommand(SampleUsers.ADMIN, active.getId().value()));

        assertThat(result).isEqualTo(EnableCommerceResult.ALREADY_ENABLED);
        assertThat(active.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
        assertThat(active.getUpdatedAt()).isEqualTo(updatedAtBefore);
    }

    @ParameterizedTest
    @MethodSource("nonAdminUsers")
    void rejectsAUserThatIsNotAnAdmin(UUID requesterId) {
        addProduct(commerce, 5);

        Throwable thrown = catchThrowable(() -> enableCommerce.execute(command(requesterId)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @Test
    void rejectsAnUnknownCommerce() {
        Throwable thrown = catchThrowable(() -> enableCommerce.execute(
                new EnableCommerceCommand(SampleUsers.ADMIN, UUID.randomUUID())));

        assertThat(thrown).isInstanceOf(CommerceNotFoundException.class);
    }

    private EnableCommerceCommand command(UUID requesterId) {
        return new EnableCommerceCommand(requesterId, commerce.getId().value());
    }

    private Product addProduct(Commerce owner, int stock) {
        Product product = Product.register(
                owner.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));
        product.adjustStock(stock);
        return productRepository.save(product);
    }
}