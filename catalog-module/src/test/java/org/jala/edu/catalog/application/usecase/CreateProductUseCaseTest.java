package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CreateProductUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();
    private final CreateProductUseCase createProduct = new CreateProductUseCase(
            productRepository, commerceRepository, new FallbackUserService());

    private Commerce commerce;

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Pizzeria"));
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void storesTheProductInTheSelectedCommerceAndReturnsItsId(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        UUID id = createProduct.execute(command);

        // Assert
        Product stored = productRepository.findById(new ProductId(id)).orElseThrow();
        assertThat(stored.getCommerceId()).isEqualTo(commerce.getId().value());
        assertThat(stored.getName()).isEqualTo("Pizza");
        assertThat(stored.getCategory()).isEqualTo("Food");
        assertThat(stored.getPrice()).isEqualByComparingTo("50.00");
    }

    @Test
    void acceptsADisabledCommerceSoItCanGetItsFirstProduct() {
        Commerce disabled = commerceRepository.save(CatalogFixtures.disabledCommerce("New commerce"));
        CreateProductCommand command = new CreateProductCommand(
                SampleUsers.ADMIN, disabled.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));

        UUID id = createProduct.execute(command);

        Product stored = productRepository.findById(new ProductId(id)).orElseThrow();
        assertThat(stored.getCommerceId()).isEqualTo(disabled.getId().value());
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsAUserWithoutPermissionAndStoresNothing(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, commerce.getId().value(), "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsAnUnknownCommerceAndStoresNothing() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                SampleUsers.ADMIN, UUID.randomUUID(), "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(CommerceNotFoundException.class);
        assertThat(productRepository.findAll()).isEmpty();
    }

    @Test
    void rejectsAMissingCommerceAndStoresNothing() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                SampleUsers.ADMIN, null, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage("Commerce is required");
        assertThat(productRepository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void doesNotStoreAProductThatBreaksADomainRule(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, commerce.getId().value(), "Pizza", "Food", BigDecimal.ZERO);

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(productRepository.findAll()).isEmpty();
    }
}