package org.jala.edu.payment.application.usecase;

import org.jala.edu.payment.application.dto.RegisterPaymentMethodCommand;
import org.jala.edu.payment.domain.exception.DomainException;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.PaymentType;
import org.jala.edu.payment.domain.port.UserService;
import org.jala.edu.payment.domain.repository.PaymentMethodRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// Caso de uso principal para la gestión de métodos de pago (HU-01 / CU-01).
 */
public class ManagePaymentMethodUseCase {

    private final PaymentMethodRepository repository;
    private final UserService userService;

    public ManagePaymentMethodUseCase(PaymentMethodRepository repository, UserService userService) {
        this.repository = Objects.requireNonNull(repository, "El repositorio no puede ser nulo");
        this.userService = Objects.requireNonNull(userService, "El servicio de usuario no puede ser nulo");
    }

    /**
     * Registra un nuevo método de pago aplicando todas las reglas de negocio de HU-01.
     */
    public PaymentMethod registerPaymentMethod(RegisterPaymentMethodCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");

        // CA4 (D-07): Validar usuario activo con rol CUSTOMER
        if (!userService.isUserActiveAndHasRole(command.getUserId(), "CUSTOMER")) {
            throw new DomainException("User not authorized for this operation");
        }

        // CA3: Validar tipo de pago no nulo/vacío y tipo válido
        if (command.getType() == null || command.getType().isBlank()) {
            throw new DomainException("El tipo de pago no puede estar vacío");
        }

        PaymentType type;
        try {
            type = PaymentType.valueOf(command.getType().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Tipo de pago inválido. Permitidos: TARJETA, TRANSFERENCIA");
        }

        // CA3: Instanciar Alias (lanza DomainException si es nulo o fuera de rango 3-30 chars)
        Alias alias = new Alias(command.getAlias());

        // CA2 (D-04): Desmarcar flag 'isLastRegistered' de métodos anteriores antes de guardar el nuevo
        repository.clearLastRegisteredFlag(command.getUserId());

        // CA1 & CA2: Crear entidad marcando isLastRegistered = true
        PaymentMethod newMethod = PaymentMethod.createNew(
                command.getUserId(),
                alias,
                type,
                command.getRawNumber()
        );

        // CA1: Guardar y retornar
        return repository.save(newMethod);
    }

    /**
     * Consulta la lista de métodos de pago de un usuario (CA4).
     */
    public List<PaymentMethod> listPaymentMethods(UUID userId) {
        Objects.requireNonNull(userId, "El userId no puede ser nulo");

        // CA4 (D-07): Validar usuario activo con rol CUSTOMER
        if (!userService.isUserActiveAndHasRole(userId, "CUSTOMER")) {
            throw new DomainException("User not authorized for this operation");
        }

        return repository.findByUserId(userId);
    }
}