package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.RegisterCommerceCommand;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RegisterCommerceUseCaseTest {

    private static final String NAME = "My Commerce";
    private static final String ADDRESS = "Calle 1 # 2-3";
    private static final String PHONE = "+573001234567";

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();

    private final RegisterCommerceUseCase registerCommerce =
            new RegisterCommerceUseCase(repository, new FallbackUserService());

    static Stream<UUID> nonAdminUsers() {
        return Stream.of(SampleUsers.MERCHANT, SampleUsers.CUSTOMER, SampleUsers.COURIER,
                UUID.randomUUID());
    }

    static Stream<UUID> usersThatAreNotAnActiveMerchant() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.CUSTOMER, SampleUsers.COURIER,
                SampleUsers.INACTIVE_CUSTOMER, UUID.randomUUID());
    }

    static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of(" ", ADDRESS, PHONE, "Commerce name cannot be blank"),
                Arguments.of(NAME, " ", PHONE, "Commerce address cannot be blank"),
                Arguments.of(NAME, ADDRESS, " ", "Commerce phone cannot be blank"),
                Arguments.of(NAME, ADDRESS, "3001234567",
                        "Commerce phone has an invalid format: 3001234567"),
                Arguments.of(NAME, ADDRESS, "+57 300 abc",
                        "Commerce phone has an invalid format: +57 300 abc"));
    }

    @Test
    void anAdminRegistersADisabledCommerce() {
        UUID id = registerCommerce.execute(
                new RegisterCommerceCommand(
                        SampleUsers.ADMIN, NAME, ADDRESS, PHONE, SampleUsers.MERCHANT));

        Commerce stored = repository.findById(new CommerceId(id)).orElseThrow();
        assertThat(stored.getName()).isEqualTo(NAME);
        assertThat(stored.getAddress()).isEqualTo(ADDRESS);
        assertThat(stored.getPhone().value()).isEqualTo(PHONE);
        assertThat(stored.getMerchantId()).isEqualTo(SampleUsers.MERCHANT);
        assertThat(stored.getStatus()).isEqualTo(CommerceStatus.DISABLED);
    }

    @ParameterizedTest
    @MethodSource("nonAdminUsers")
    void rejectsAUserThatIsNotAnAdminAndStoresNothing(UUID requesterId) {
        Throwable thrown = catchThrowable(() -> registerCommerce.execute(
                new RegisterCommerceCommand(
                        requesterId, NAME, ADDRESS, PHONE, SampleUsers.MERCHANT)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(repository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidData")
    void rejectsInvalidDataNamingTheWrongFieldAndStoresNothing(
            String name, String address, String phone, String expectedMessage) {
        Throwable thrown = catchThrowable(() -> registerCommerce.execute(
                new RegisterCommerceCommand(
                        SampleUsers.ADMIN, name, address, phone, SampleUsers.MERCHANT)));

        assertThat(thrown).isInstanceOf(DomainException.class).hasMessage(expectedMessage);
        assertThat(repository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("usersThatAreNotAnActiveMerchant")
    void rejectsAnOwnerThatIsNotAnActiveMerchantAndStoresNothing(UUID merchantId) {
        Throwable thrown = catchThrowable(() -> registerCommerce.execute(
                new RegisterCommerceCommand(SampleUsers.ADMIN, NAME, ADDRESS, PHONE, merchantId)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("The owner must be an active merchant");
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void rejectsACommerceWithoutOwnerAndStoresNothing() {
        Throwable thrown = catchThrowable(() -> registerCommerce.execute(
                new RegisterCommerceCommand(SampleUsers.ADMIN, NAME, ADDRESS, PHONE, null)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce merchant is required");
        assertThat(repository.findAll()).isEmpty();
    }
}