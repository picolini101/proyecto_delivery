
package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.port.in.AssignedOrderCommand;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;

public class MarkOrderAssignedUseCase implements UseCase<AssignedOrderCommand, Void> {
    private final OrderRepositoryPort orderRepository;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    public MarkOrderAssignedUseCase(OrderRepositoryPort orderRepository, ChangeOrderStatusUseCase changeOrderStatusUseCase) {
        this.orderRepository = orderRepository;
        this.changeOrderStatusUseCase = changeOrderStatusUseCase;
    }
    @Override
    public Void execute(AssignedOrderCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = orderRepository.findById(orderId).orElseThrow();

        order.assignCourier(command.courierId());
        orderRepository.save(order);

        ChangeOrderStatusCommand statusCommand = buildStatusCommand(command);
        return changeOrderStatusUseCase.execute(statusCommand);
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
