package org.jala.edu.delivery.application.usecase;

import org.jala.edu.delivery.domain.exception.ConcurrencyException;
import org.jala.edu.delivery.domain.model.DeliveryOrder;
import org.jala.edu.delivery.application.port.out.DeliveryRepositoryPort;

import java.util.Optional;

public class AssignAtomicOrderUseCaseTest {

  public static void main(String[] args) {
    AssignAtomicOrderUseCaseTest test = new AssignAtomicOrderUseCaseTest();
    test.shouldThrowConcurrencyExceptionWhenOrderIsAlreadyAssigned();
    System.out.println("Todas las pruebas pasaron");
  }

  public void shouldThrowConcurrencyExceptionWhenOrderIsAlreadyAssigned() {
    DeliveryRepositoryPort fakeRepository = new FakeDeliveryRepository();
    AssignAtomicOrderUseCase useCase = new AssignAtomicOrderUseCase(fakeRepository);

    try {
      useCase.assignOrder("order-1", "courier-new");
      throw new RuntimeException("PRUEBA FALLIDA: Se esperaba ConcurrencyException pero no ocurrió.");
    } catch (ConcurrencyException e) {
      if (!"El pedido ya ha sido tomado por otro repartidor.".equals(e.getMessage())) {
        throw new RuntimeException("PRUEBA FALLIDA: El mensaje de la excepción no coincide.");
      }
    }
  }

  private static class FakeDeliveryRepository implements DeliveryRepositoryPort {
    @Override
    public Optional<DeliveryOrder> findById(String id) {
      DeliveryOrder order = new DeliveryOrder("order-1", "courier-prev");
      order.setAssigned(true);
      return Optional.of(order);
    }

    @Override
    public DeliveryOrder save(DeliveryOrder order) {
      return order;
    }
  }
}