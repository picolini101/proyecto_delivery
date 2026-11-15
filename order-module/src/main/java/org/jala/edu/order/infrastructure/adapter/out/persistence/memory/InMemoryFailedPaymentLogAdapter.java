package org.jala.edu.order.infrastructure.adapter.out.persistence.memory;

import java.util.List;

import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.order.application.port.out.FailedPaymentLogPort;
import org.jala.edu.order.domain.model.FailedPaymentRecord;
import org.jala.edu.order.domain.model.FailedPaymentRecordId;
import org.jala.edu.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

/** Guarda los intentos de pago fallidos en memoria (se pierden al cerrar la aplicacion). */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryFailedPaymentLogAdapter extends InMemoryCrudRepository<FailedPaymentRecord, FailedPaymentRecordId>
        implements FailedPaymentLogPort {

    @Override
    public List<FailedPaymentRecord> findByOrderId(OrderId orderId) {
        return stream()
                .filter(record -> record.getOrderId().equals(orderId))
                .toList();
    }
}