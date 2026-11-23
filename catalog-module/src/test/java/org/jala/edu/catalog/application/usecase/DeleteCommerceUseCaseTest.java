
package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.CatalogFixtures;
import org.jala.edu.catalog.application.port.in.DeleteCommerceCommand;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.Product;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryProductRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DeleteCommerceUseCaseTest {

    private final InMemoryProductRepositoryAdapter productRepository =
            new InMemoryProductRepositoryAdapter();
    private final InMemoryCommerceRepositoryAdapter commerceRepository =
            new InMemoryCommerceRepositoryAdapter();

    private final DeleteCommerceUseCase deleteCommerce = new DeleteCommerceUseCase(
            commerceRepository, productRepository, new FallbackUserService());

    private Commerce commerce;

    static Stream<UUID> nonAdminUsers() {
        return Stream.of(
                SampleUsers.MERCHANT,
                SampleUsers.CUSTOMER,
                SampleUsers.COURIER,
                UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        commerce = commerceRepository.save(CatalogFixtures.activeCommerce("Pizzeria"));
    }

    @Test
    void deletesCommerceAndAllItsProductsWhenRequesterIsAdmin() {
        Product pizza = saveProduct(commerce, "Pizza", "Comida", 10);
        Product pasta = saveProduct(commerce, "Pasta", "Comida", 5);

        deleteCommerce.execute(commandFor(SampleUsers.ADMIN, commerce));

        assertThat(commerceRepository.existsById(commerce.getId())).isFalse();
        assertThat(productRepository.findByCommerceId(commerce.getId().value())).isEmpty();
        assertThat(productRepository.existsById(pizza.getId())).isFalse();
        assertThat(productRepository.existsById(pasta.getId())).isFalse();
    }

    @ParameterizedTest
    @MethodSource("nonAdminUsers")
    void rejectsUsersWhoAreNotAdminAndKeepsCommerceAndProducts(UUID requesterId) {
        Product pizza = saveProduct(commerce, "Pizza", "Comida", 5);

        Throwable thrown = catchThrowable(
                () -> deleteCommerce.execute(commandFor(requesterId, commerce)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(commerceRepository.existsById(commerce.getId())).isTrue();
        assertThat(productRepository.existsById(pizza.getId())).isTrue();
    }

    @Test
    void reportsWhenCommerceDoesNotExistAndChangesNothing() {
        Product pizza = saveProduct(commerce, "Pizza", "Comida", 5);

        Throwable thrown = catchThrowable(() -> deleteCommerce.execute(
                new DeleteCommerceCommand(SampleUsers.ADMIN, UUID.randomUUID())));

        assertThat(thrown).isInstanceOf(CommerceNotFoundException.class)
                .hasMessageStartingWith("Commerce not found");
        assertThat(commerceRepository.existsById(commerce.getId())).isTrue();
        assertThat(productRepository.existsById(pizza.getId())).isTrue();
    }

    @Test
    void keepsProductsFromOtherCommercesUnchanged() {
        Commerce otherCommerce =
                commerceRepository.save(CatalogFixtures.activeCommerce("Hamburguesería"));

        Product pizza = saveProduct(commerce, "Pizza", "Comida", 10);
        Product burger = saveProduct(otherCommerce, "Hamburguesa", "Comida", 7);

        deleteCommerce.execute(commandFor(SampleUsers.ADMIN, commerce));

        assertThat(commerceRepository.existsById(otherCommerce.getId())).isTrue();
        assertThat(productRepository.existsById(pizza.getId())).isFalse();

        Product storedBurger = productRepository.findById(burger.getId()).orElseThrow();
        assertThat(storedBurger.getName()).isEqualTo("Hamburguesa");
        assertThat(storedBurger.getCategory()).isEqualTo("Comida");
        assertThat(storedBurger.getStock()).isEqualTo(7);
    }

    @Test
    void restoresCommerceAndProductsWhenProductDeletionFails() {
        InMemoryProductRepositoryAdapter failingRepository =
                new InMemoryProductRepositoryAdapter() {
                    @Override
                    public void deleteAllByCommerceId(UUID commerceId) {
                        super.deleteAllByCommerceId(commerceId);
                        throw new IllegalStateException("Deletion failed");
                    }
                };

        Product pizza = saveProduct(commerce, "Pizza", "Comida", 10);
        failingRepository.save(pizza);

        DeleteCommerceUseCase useCase = new DeleteCommerceUseCase(
                commerceRepository, failingRepository, new FallbackUserService());

        Throwable thrown = catchThrowable(
                () -> useCase.execute(commandFor(SampleUsers.ADMIN, commerce)));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deletion failed");

        assertThat(commerceRepository.existsById(commerce.getId())).isTrue();
        assertThat(failingRepository.existsById(pizza.getId())).isTrue();
    }

    private Product saveProduct(Commerce owner, String name, String category, int stock) {
        Product product = Product.register(
                owner.getId().value(), name, category, new BigDecimal("50.00"));
        product.adjustStock(stock);
        return productRepository.save(product);
    }

    private static DeleteCommerceCommand commandFor(UUID requesterId, Commerce commerce) {
        return new DeleteCommerceCommand(requesterId, commerce.getId().value());
    }
}
