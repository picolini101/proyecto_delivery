package org.jala.edu.payment.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Estado del pago de un pedido (HU #31).
// orderId: pedido consultado
// status: APROBADO, RECHAZADO o PENDIENTE
// amount: monto del intento mostrado; nulo si el pedido aun no tiene intentos
// attemptedAt fecha y hora del intento mostrado; nula si el pedido aun no tiene intentos

public record PaymentStatusResult(UUID orderId, String status, BigDecimal amount, Instant attemptedAt) {

    public static final String APPROVED = "APROBADO";
    public static final String REJECTED = "RECHAZADO";
    public static final String PENDING = "PENDIENTE";
}