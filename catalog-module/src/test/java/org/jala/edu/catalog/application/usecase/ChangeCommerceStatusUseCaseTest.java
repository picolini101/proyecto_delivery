package org.jala.edu.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.UUID;
import java.util.stream.Stream;
import org.jala.edu.catalog.application.port.in.ChangeCommerceStatusCommand;
import org.jala.edu.catalog.domain.exception.CommerceNotFoundException;
import org.jala.edu.catalog.domain.model.Commerce;
import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.catalog.domain.model.CommerceStatus;
import org.jala.edu.catalog.infrastructure.adapter.out.persistence.memory.InMemoryCommerceRepositoryAdapter;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

class ChangeCommerceStatusUseCaseTest {

    private final InMemoryCommerceRepositoryAdapter repository =
            new InMemoryCommerceRepositoryAdapter();

    private final ChangeCommerceStatusUseCase changeStatus =
            new ChangeCommerceStatusUseCase(repository, new FallbackUserService());

    static Stream<UUID> authorizedUsers() {
        return Stream.of(SampleUsers.ADMIN, SampleUsers.MERCHANT);
    }

    static Stream<UUID> unauthorizedUsers() {
        return Stream.of(SampleUsers.CUSTOMER, SampleUsers.COURIER, UUID.randomUUID());
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void disablesAnActiveCommerce(UUID requesterId) {
        Commerce commerce = repository.save(Commerce.register("My Commerce"));

        changeStatus.execute(new ChangeCommerceStatusCommand(
                requesterId, commerce.getId().value(), CommerceStatus.DISABLED));

        assertThat(repository.findById(commerce.getId()).orElseThrow().getStatus())
                .isEqualTo(CommerceStatus.DISABLED);
    }

    @ParameterizedTest
    @MethodSource("authorizedUsers")
    void activatesADisabledCommerce(UUID requesterId) {
        Commerce commerce = Commerce.register("My Commerce");
        commerce.disable();
        repository.save(commerce);

        changeStatus.execute(new ChangeCommerceStatusCommand(
                requesterId, commerce.getId().value(), CommerceStatus.ACTIVE));

        assertThat(repository.findById(commerce.getId()).orElseThrow().getStatus())
                .isEqualTo(CommerceStatus.ACTIVE);
    }

    @ParameterizedTest
    @MethodSource("unauthorizedUsers")
    void rejectsUnauthorizedUsers(UUID requesterId) {
        Commerce commerce = repository.save(Commerce.register("My Commerce"));

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
        Commerce commerce = repository.save(Commerce.register("My Commerce"));

        Throwable thrown = catchThrowable(() -> changeStatus.execute(
                new ChangeCommerceStatusCommand(
                        SampleUsers.ADMIN, commerce.getId().value(), null)));

        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("Commerce status cannot be null");
        assertThat(commerce.getStatus()).isEqualTo(CommerceStatus.ACTIVE);
    }
}
