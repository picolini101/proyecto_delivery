
package org.jala.edu.order.application.usecase.orders;

import java.util.List;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.Order;

public class FindReadyForDeliveryUseCase implements UseCase<Void, List<ReadyOrder>> {
    private final OrderRepositoryPort orderRepository;
    public FindReadyForDeliveryUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }
    @Override
    public List<ReadyOrder> execute(Void unused) {
        return orderRepository.findAll().stream()
                .map(this::toReadyOrder)
                .toList();
    }
    private ReadyOrder toReadyOrder(Order order) {
        return new ReadyOrder(
                order.getId().value(),
                order.getDeliveryAddress().toString(),
                order.getShippingCost().value()
        );
    }

}

