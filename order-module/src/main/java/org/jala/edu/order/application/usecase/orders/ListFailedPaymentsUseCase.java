package org.jala.edu.order.application.usecase.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.dto.FailedPaymentItem;
import org.jala.edu.order.application.port.out.FailedPaymentLogPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;

/**
 * Lista los pedidos que el cliente tiene cancelados por pago rechazado, del mas nuevo al mas antiguo.
 * Une dos datos que ya se guardan: el intento fallido (detalle y hora) y el motivo del historial del pedido.
 */
public class ListFailedPaymentsUseCase implements UseCase<UUID, List<FailedPaymentItem>> {

    /** Donde estan guardados los intentos de pago fallidos (auditoria). */
    private final FailedPaymentLogPort failedPayments;

    /** Para leer el motivo con que se cancelo cada pedido. */
    private final OrderStatusHistoryPort history;

    public ListFailedPaymentsUseCase(FailedPaymentLogPort failedPayments, OrderStatusHistoryPort history) {
        this.failedPayments = failedPayments;
        this.history = history;
    }

    @Override
    public List<FailedPaymentItem> execute(UUID customerId) {
        // findAll() devuelve del mas antiguo al mas nuevo; se invierte para mostrar primero el mas nuevo
        List<FailedPaymentRecord> mine = new ArrayList<>(failedPayments.findAll().stream()
                .filter(record -> record.getCustomerId().equals(customerId))
                .toList());
        Collections.reverse(mine);
        return mine.stream().map(this::toItem).toList();
    }

    private FailedPaymentItem toItem(FailedPaymentRecord record) {
        return new FailedPaymentItem(
                record.getOrderId().value(),
                cancellationReason(record),
                record.getReason(),
                record.getCreatedAt());
    }

    /** Motivo del ultimo cambio a CANCELLED del pedido; {@code null} si no hay. */
    private String cancellationReason(FailedPaymentRecord record) {
        return history.findByOrderId(record.getOrderId()).stream()
                .filter(change -> change.getNewStatus() == OrderStatus.CANCELLED)
                .reduce((first, last) -> last)
                .map(OrderStatusChange::getReason)
                .orElse(null);
    }
}
