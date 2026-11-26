package org.jala.edu.order.application.port.out;

import java.util.List;

import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.FailedPaymentRecordId;
import org.jala.edu.order.domain.model.OrderId;

/**
 * Puerto de salida: define QUE se necesita guardar (los intentos de pago fallidos para auditoria),
 * sin decir COMO. Lo implementan un adaptador en memoria y otro JPA (base de datos).
 */
public interface FailedPaymentLogPort extends CrudRepositoryPort<FailedPaymentRecord, FailedPaymentRecordId> {

    /** @return los intentos fallidos de un pedido, del mas antiguo al mas nuevo */
    List<FailedPaymentRecord> findByOrderId(OrderId orderId);
}