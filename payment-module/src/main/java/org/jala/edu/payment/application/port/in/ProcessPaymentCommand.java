package org.jala.edu.payment.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
        UUID orderId,
        UUID userId,
        UUID paymentMethodId,
        BigDecimal amount
) {
}