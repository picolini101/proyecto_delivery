package org.jala.edu.payment.application.usecase;

import org.jala.edu.payment.application.port.in.RegisterPaymentMethodCommand;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.jala.edu.payment.domain.port.UserService;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import java.util.List;
import java.util.Objects;
import java.util.UUID;



public class ManagePaymentMethodUseCase {

    private final PaymentMethodRepositoryPort repository;
    private final UserService userService;

    public ManagePaymentMethodUseCase(PaymentMethodRepositoryPort repository, UserService userService) {
        this.repository = Objects.requireNonNull(repository, "El repositorio no puede ser nulo");
        this.userService = Objects.requireNonNull(userService, "El servicio de usuario no puede ser nulo");
    }

    public PaymentMethod registerPaymentMethod(RegisterPaymentMethodCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");

        // CA4 (D-07): Validar usuario activo con rol CUSTOMER
        if (!userService.isUserActiveAndHasRole(command.getUserId(), "CUSTOMER")) {
            throw new DomainException("User not authorized for this operation");
        }

        if (command.getType() == null || command.getType().isBlank()) {
            throw new DomainException("El tipo de pago no puede estar vacío");
        }

        PaymentType type;
        try {
            type = PaymentType.valueOf(command.getType().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Tipo de pago inválido. Permitidos: TARJETA, TRANSFERENCIA");
        }

        Alias alias = new Alias(command.getAlias());

        // CA2 (D-04): Desmarcar el flag previo
        repository.clearLastRegisteredFlag(command.getUserId());

        PaymentMethod newMethod = PaymentMethod.createNew(
                command.getUserId(),
                alias,
                type,
                command.getRawNumber()
        );

        return repository.save(newMethod);
    }

    public List<PaymentMethod> listPaymentMethods(UUID userId) {
        Objects.requireNonNull(userId, "El userId no puede ser nulo");

        if (!userService.isUserActiveAndHasRole(userId, "CUSTOMER")) {
            throw new DomainException("User not authorized for this operation");
        }

        return repository.findByUserId(userId);
    }
}