package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.MarkOrderReadyForDeliveryCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;

public class MarkOrderReadyForDeliveryUseCase implements UseCase<MarkOrderReadyForDeliveryCommand, Void> {

    private final OrderRepositoryPort orderRepository;
    private final ChangeOrderStatusUseCase changeOrderStatus;

    public MarkOrderReadyForDeliveryUseCase(OrderRepositoryPort orderRepository,
                                            ChangeOrderStatusUseCase changeOrderStatus) {
        this.orderRepository = orderRepository;
        this.changeOrderStatus = changeOrderStatus;
    }

    @Override
    public Void execute(MarkOrderReadyForDeliveryCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));
        if (order.getLines().stream().noneMatch(line -> line.getCommerceId().equals(command.merchantId()))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece a este comercio.");
        }

        return changeOrderStatus.execute(new ChangeOrderStatusCommand(
                command.orderId(),
                OrderStatus.READY_FOR_DELIVERY,
                command.changedById(),
                "Merchant marked order ready for delivery"));
    }
}
