package org.jala.edu.order.infrastructure.adapter.in.contract;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.order.application.port.in.AssignedOrderCommand;
import org.jala.edu.order.application.port.in.MarkOrderCommand;
import org.jala.edu.order.application.usecase.orders.FindReadyForDeliveryUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderAssignedUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderDeliveredUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderOnTheWayUseCase;

import java.util.List;
import java.util.UUID;

/** Adapts the shared delivery contract to order application use cases. */
public class OrderServiceAdapter implements OrderService {

    private final FindReadyForDeliveryUseCase findReadyForDelivery;
    private final MarkOrderAssignedUseCase markOrderAssigned;
    private final MarkOrderOnTheWayUseCase markOrderOnTheWay;
    private final MarkOrderDeliveredUseCase markOrderDelivered;
    private final CurrentUserService currentUserService;

    public OrderServiceAdapter(
            FindReadyForDeliveryUseCase findReadyForDelivery,
            MarkOrderAssignedUseCase markOrderAssigned,
            MarkOrderOnTheWayUseCase markOrderOnTheWay,
            MarkOrderDeliveredUseCase markOrderDelivered,
            CurrentUserService currentUserService) {
        this.findReadyForDelivery = findReadyForDelivery;
        this.markOrderAssigned = markOrderAssigned;
        this.markOrderOnTheWay = markOrderOnTheWay;
        this.markOrderDelivered = markOrderDelivered;
        this.currentUserService = currentUserService;
    }

    @Override
    public List<ReadyOrder> findReadyForDelivery() {
        return findReadyForDelivery.execute(null);
    }

    @Override
    public void assign(UUID orderId, UUID courierId) {
        markOrderAssigned.execute(new AssignedOrderCommand(orderId, courierId, currentUserService.currentUser().id()));
    }

    @Override
    public void markOnTheWay(UUID orderId) {
        markOrderOnTheWay.execute(new MarkOrderCommand(orderId, currentUserService.currentUser().id()));
    }

    @Override
    public void markDelivered(UUID orderId) {
        markOrderDelivered.execute(new MarkOrderCommand(orderId, currentUserService.currentUser().id()));
    }
}
