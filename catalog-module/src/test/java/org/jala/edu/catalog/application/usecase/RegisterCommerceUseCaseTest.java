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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RegisterCommerceUseCaseTest {

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();

    private final RegisterCommerceUseCase registerCommerce =
            new RegisterCommerceUseCase(repository, new FallbackUserService());

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void registersAnActiveCommerceAndReturnsItsId(UUID requesterId) {
        RegisterCommerceCommand command =
                new RegisterCommerceCommand(requesterId, "My Commerce");

        UUID id = registerCommerce.execute(command);

        Commerce stored = repository.findById(new CommerceId(id)).orElseThrow();
        assertThat(stored.getName()).isEqualTo("My Commerce");
        assertThat(stored.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsUnauthorizedUsersAndStoresNothing(UUID requesterId) {
        RegisterCommerceCommand command =
                new RegisterCommerceCommand(requesterId, "My Commerce");

        Throwable thrown = catchThrowable(() -> registerCommerce.execute(command));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("User not authorized for this operation");
        assertThat(repository.findAll()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void rejectsAnInvalidCommerceName(UUID requesterId) {
        RegisterCommerceCommand command =
                new RegisterCommerceCommand(requesterId, " ");

        Throwable thrown = catchThrowable(() -> registerCommerce.execute(command));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce name cannot be blank");
        assertThat(repository.findAll()).isEmpty();
    }
}
