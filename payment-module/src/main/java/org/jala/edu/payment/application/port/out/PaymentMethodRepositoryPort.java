package org.jala.edu.payment.application.port.out;

import org.jala.edu.payment.domain.model.PaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepositoryPort {

    // Guarda o actualiza un método de pagp.
    PaymentMethod save(PaymentMethod paymentMethod);

    //Busca un método de pago por su ID.
    Optional<PaymentMethod> findById(UUID id);

    // Obtiene la lista completa de métodos de pago asociados a un usuario.
    List<PaymentMethod> findByUserId(UUID userId);

    //Obtiene el último método de pago registrado por el usuario.
    Optional<PaymentMethod> findLastRegisteredByUserId(UUID userId);

    // Desmarca el flag 'isLastRegistered' de todos los métodos de pago de un usuario
    void clearLastRegisteredFlag(UUID userId);
}
