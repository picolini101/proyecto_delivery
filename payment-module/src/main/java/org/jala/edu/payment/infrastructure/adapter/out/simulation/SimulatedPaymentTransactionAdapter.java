package org.jala.edu.payment.infrastructure.adapter.out.simulation;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.payment.application.port.out.PaymentTransactionPort;

public class SimulatedPaymentTransactionAdapter
        implements PaymentTransactionPort {

    @Override
    public String recordApproved(
            UUID orderId,
            UUID userId,
            BigDecimal amount) {

        String reference = UUID.randomUUID().toString();

        System.out.println("Pago simulado aprobado: " + reference);

        return reference;
    }

    @Override
    public void recordRejected(
            UUID orderId,
            UUID userId,
            BigDecimal amount,
            String reason) {

        System.out.println("Pago simulado rechazado: " + reason);
    }
}