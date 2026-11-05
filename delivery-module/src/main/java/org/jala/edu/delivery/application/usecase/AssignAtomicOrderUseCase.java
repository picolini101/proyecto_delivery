package org.jala.edu.delivery.application.usecase;

import org.jala.edu.delivery.domain.exception.ConcurrencyException;
import org.jala.edu.delivery.domain.model.DeliveryOrder;
import org.jala.edu.delivery.application.port.out.DeliveryRepositoryPort;

public class AssignAtomicOrderUseCase {
  private final DeliveryRepositoryPort deliveryRepository;

  public AssignAtomicOrderUseCase(DeliveryRepositoryPort deliveryRepository) {
    this.deliveryRepository = deliveryRepository;
  }

  public DeliveryOrder assignOrder(String orderId, String courierId) {
    DeliveryOrder order = deliveryRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("El pedido no existe."));

    synchronized (this) {
      if (order.isAssigned()) {
        throw new ConcurrencyException("El pedido ya ha sido tomado por otro repartidor.");
      }

      order.assignToCourier(courierId);

      return deliveryRepository.save(order);
    }
  }
}
