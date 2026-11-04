package org.jala.edu.delivery.application.usecase;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.delivery.application.port.in.RegisterCourierCommand;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.domain.model.Courier;

/**
 * Use case UC-01: register a delivery driver
 */
public class RegisterCourierUseCaseTest
        implements UseCase<RegisterCourierCommand, Courier> {

    private final CourierRepositoryPort couriers;
    private final UserService users;

    public RegisterCourierUseCaseTest(
            CourierRepositoryPort couriers,
            UserService users) {
        this.couriers = couriers;
        this.users = users;
    }

    @Override
    public Courier execute(RegisterCourierCommand command) {

        if (command == null) {
            throw new DomainException("Los datos son obligatorios");
        }

        if (command.adminId() == null
                || !users.isActiveWithRole(command.adminId(), UserRole.ADMIN)) {
            throw new DomainException(
                    "Solo un administrador activo puede registrar repartidores");
        }

        Courier courier = Courier.register(
                command.name(),
                command.phoneNumber(),
                command.transportMode()
        );

        return couriers.save(courier);
    }
}