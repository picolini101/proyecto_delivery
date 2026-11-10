package org.jala.edu.payment.infrastructure.adapter.out.simulation;

import java.util.UUID;
import org.jala.edu.payment.application.port.out.PaymentMethodValidationPort;

public class SimulatedPaymentMethodAdapter
        implements PaymentMethodValidationPort {

    @Override
    public boolean isValidPaymentMethod(
            UUID userId,
            UUID paymentMethodId) {

        return userId != null && paymentMethodId != null;
    }
}