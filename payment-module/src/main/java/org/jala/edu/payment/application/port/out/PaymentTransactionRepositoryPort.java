package org.jala.edu.payment.application.port.out;

import org.jala.edu.payment.domain.model.PaymentTransaction;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida para guardar y consultar transacciones de pago.
 */
public interface PaymentTransactionRepositoryPort {
    PaymentTransaction save(PaymentTransaction transaction);
    List<PaymentTransaction> findByUserId(UUID userId);
    boolean existsApprovedByOrderId(UUID orderId);
}
