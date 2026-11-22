
package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.port.in.AssignedOrderCommand;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;

import java.util.UUID;

public class MarkOrderAssignedUseCase implements UseCase<AssignedOrderCommand, Void> {
    private final OrderRepositoryPort orderRepository;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    public MarkOrderAssignedUseCase(OrderRepositoryPort orderRepository, ChangeOrderStatusUseCase changeOrderStatusUseCase) {
        this.orderRepository = orderRepository;
        this.changeOrderStatusUseCase = changeOrderStatusUseCase;
    }
    @Override
    public Void execute(AssignedOrderCommand command) {
        Order order = findOrder(command.orderId());
        validateOrderCanBeAssigned(order);

        order.assignCourier(command.courierId());
        orderRepository.save(order);

        ChangeOrderStatusCommand statusCommand = buildStatusCommand(command);
        return changeOrderStatusUseCase.execute(statusCommand);
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(OrderId.of(orderId)).orElseThrow();
    }

    private void validateOrderCanBeAssigned(Order order) {
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new InvalidStateTransitionException(
                    "Only READY_FOR_DELIVERY as the previous status"
            );
        }
    }
    private ChangeOrderStatusCommand buildStatusCommand(AssignedOrderCommand command) {
        return new ChangeOrderStatusCommand(
                command.orderId(),
                OrderStatus.ASSIGNED,
                command.changedById(),
                "Transition to Assigned"
        );
    }
}
