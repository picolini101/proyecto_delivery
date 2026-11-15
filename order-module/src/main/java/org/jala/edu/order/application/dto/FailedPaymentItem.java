package org.jala.edu.order.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Un pedido cancelado por pago rechazado, listo para mostrarse al cliente.
 *
 * @param orderId            id del pedido cancelado
 * @param cancellationReason motivo guardado en el historial del pedido ("Pago rechazado")
 * @param paymentDetail      detalle que entrego el servicio de pagos; puede ser nulo
 * @param attemptedAt        fecha y hora del intento fallido
 */
public record FailedPaymentItem(UUID orderId, String cancellationReason, String paymentDetail, Instant attemptedAt) {
}
