package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierAvailabilityAdapter;
import org.junit.jupiter.api.Test;

class ValidateCourierEligibilityUseCaseTest {

    private final InMemoryCourierAvailabilityAdapter availability = new InMemoryCourierAvailabilityAdapter();
    private final ValidateCourierEligibilityUseCase validateEligibility =
            new ValidateCourierEligibilityUseCase(new FallbackUserService(), availability);

    @Test
    void acceptsAnActiveCourierWhoIsAvailable() {
        // Act
        Throwable thrown = catchThrowable(() -> validateEligibility.execute(SampleUsers.COURIER));

        // Assert
        assertThat(thrown).isNull();
    }

    @Test
    void rejectsACourierWhoIsNotAvailable() {
        // Arrange
        availability.markUnavailable(SampleUsers.COURIER);

        // Act
        Throwable thrown = catchThrowable(() -> validateEligibility.execute(SampleUsers.COURIER));

        // Assert
        assertThat(thrown).isInstanceOf(DomainException.class)
                .hasMessage("El repartidor no está disponible para tomar pedidos");
    }

    @Test
    void rejectsAUserWhoIsNotACourier() {
        // Act
        Throwable thrown = catchThrowable(() -> validateEligibility.execute(SampleUsers.CUSTOMER));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class)
                .hasMessage("User not authorized for this operation");
    }
}
