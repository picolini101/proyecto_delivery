
package org.jala.edu.payment.infrastructure.adapter.out.simulation;

import java.util.UUID;

import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;
import org.jala.edu.payment.domain.model.PaymentMethod;

public class SimulatedPaymentMethodAdapter
        implements PaymentMethodValidationPort {

    private final PaymentMethodRepositoryPort repository;

    public SimulatedPaymentMethodAdapter(
            PaymentMethodRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public boolean isValidPaymentMethod(
            UUID userId,
            UUID paymentMethodId) {

        if (userId == null || paymentMethodId == null) {
            return false;
        }

        return repository.findById(paymentMethodId)
                .filter(method -> method.getUserId().equals(userId))
                .filter(PaymentMethod::isActive)
                .isPresent();
    }
}
