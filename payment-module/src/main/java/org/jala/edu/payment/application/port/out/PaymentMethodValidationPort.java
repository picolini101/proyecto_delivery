
package org.jala.edu.payment.application.port.out;

import java.util.UUID;

public interface PaymentMethodValidationPort {

    boolean hasActivePaymentMethod(UUID userId);
}
