package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.RegisterCourierCommand;
import org.jala.edu.delivery.domain.model.Courier;
import org.jala.edu.delivery.domain.model.TransportMode;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierRepositoryAdapter;
import org.junit.jupiter.api.Test;

class RegisterCourierUseCaseTest {

    private final InMemoryCourierRepositoryAdapter repository =
            new InMemoryCourierRepositoryAdapter();

    private final RegisterCourierUseCase registerCourier =
            new RegisterCourierUseCase(repository, new FallbackUserService());

    @Test
    void registersCourierWhenAdminIsActive() {
        // Arrange
        RegisterCourierCommand command = new RegisterCourierCommand(
                SampleUsers.ADMIN,
                "Maria Perez",
                "3001234567",
                TransportMode.MOTORCYCLE
        );

        // Act
        Courier result = registerCourier.execute(command);

        // Assert
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria Perez");
        assertThat(result.getPhoneNumber()).isEqualTo("3001234567");
        assertThat(result.getTransportMode()).isEqualTo(TransportMode.MOTORCYCLE);
        assertThat(repository.findById(result.getId())).contains(result);
    }

    @Test
    void rejectsANullCommand() {
        // Act
        Throwable thrown = catchThrowable(() -> registerCourier.execute(null));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Los datos son obligatorios.");
    }

    @Test
    void rejectsACommandWithoutAdminId() {
        // Arrange
        RegisterCourierCommand command = new RegisterCourierCommand(
                null,
                "Maria Perez",
                "3001234567",
                TransportMode.BICYCLE
        );

        // Act
        Throwable thrown = catchThrowable(() -> registerCourier.execute(command));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Solo un administrador activo puede registrar repartidores.");
    }

    @Test
    void rejectsAUserWhoIsNotAnAdmin() {
        // Arrange
        RegisterCourierCommand command = new RegisterCourierCommand(
                SampleUsers.CUSTOMER,
                "Maria Perez",
                "3001234567",
                TransportMode.BICYCLE
        );

        // Act
        Throwable thrown = catchThrowable(() -> registerCourier.execute(command));

        // Assert
        assertThat(thrown)
                .isInstanceOf(DomainException.class)
                .hasMessage("Solo un administrador activo puede registrar repartidores.");

        assertThat(repository.findAll()).isEmpty();
    }
}