package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.CreateProductCommand;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackCatalogService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CreateProductUseCaseTest {

    private static final UUID COMMERCE_ID = FallbackCatalogService.SAMPLE_COMMERCE;

    private final InMemoryProductRepositoryAdapter repository = new InMemoryProductRepositoryAdapter();
    private final CreateProductUseCase createProduct =
            new CreateProductUseCase(repository, new FallbackUserService());

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void storesTheProductInTheCommerceAndReturnsItsId(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        UUID id = createProduct.execute(command);

        // Assert
        Product stored = repository.findById(new ProductId(id)).orElseThrow();
        assertThat(stored.getCommerceId()).isEqualTo(COMMERCE_ID);
        assertThat(stored.getName()).isEqualTo("Pizza");
        assertThat(stored.getCategory()).isEqualTo("Food");
        assertThat(stored.getPrice()).isEqualByComparingTo("50.00");
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsAUserWithoutPermissionAndStoresNothing(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, COMMERCE_ID, "Pizza", "Food", new BigDecimal("50.00"));

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(repository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void doesNotStoreAProductThatBreaksADomainRule(UUID requesterId) {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                requesterId, COMMERCE_ID, "Pizza", "Food", BigDecimal.ZERO);

        // Act
        Throwable thrown = catchThrowable(() -> createProduct.execute(command));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class);
        assertThat(repository.findAll()).isEmpty();
    }
}