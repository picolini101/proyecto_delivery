package org.jala.edu.delivery.application.port.out;

import org.jala.edu.delivery.domain.model.DeliveryOrder;
import java.util.Optional;

public interface DeliveryRepositoryPort {
  Optional<DeliveryOrder> findById(String id);
  DeliveryOrder save(DeliveryOrder order);
}