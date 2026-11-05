
package org.jala.edu.payment.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentTransactionPort {

    String recordApproved(
            UUID orderId,
            UUID userId,
            BigDecimal amount
    );

    void recordRejected(
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            String reason
    );
}
