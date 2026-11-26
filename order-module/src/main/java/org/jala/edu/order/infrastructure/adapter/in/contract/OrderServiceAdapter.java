package org.jala.edu.order.infrastructure.adapter.in.contract;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.order.application.port.in.AssignedOrderCommand;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.MarkOrderCommand;

import org.jala.edu.order.application.usecase.orders.ChangeOrderStatusUseCase;
import org.jala.edu.order.application.usecase.orders.FindReadyForDeliveryUseCase;
import org.jala.edu.order.application.usecase.orders.GetOrderDetailUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderAssignedUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderOnTheWayUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderDeliveredUseCase;
import org.jala.edu.order.domain.exception.InvalidStateTransitionException;
import org.jala.edu.order.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

/** Adapts the shared delivery contract to order application use cases. */
public class OrderServiceAdapter implements OrderService {

    private final FindReadyForDeliveryUseCase findReadyForDelivery;
    private final MarkOrderAssignedUseCase markOrderAssigned;
    private final MarkOrderOnTheWayUseCase markOrderOnTheWay;
    private final MarkOrderDeliveredUseCase markOrderDelivered;
    private final ChangeOrderStatusUseCase changeOrderStatus;
    private final CurrentUserService currentUserService;
    private final GetOrderDetailUseCase getOrderDetailUseCase;

    public OrderServiceAdapter(
            FindReadyForDeliveryUseCase findReadyForDelivery,
            MarkOrderAssignedUseCase markOrderAssigned,
            MarkOrderOnTheWayUseCase markOrderOnTheWay,
            MarkOrderDeliveredUseCase markOrderDelivered,
            CurrentUserService currentUserService, GetOrderDetailUseCase getOrderDetailUseCase,
            ChangeOrderStatusUseCase changeOrderStatus) {
        this.findReadyForDelivery = findReadyForDelivery;
        this.markOrderAssigned = markOrderAssigned;
        this.markOrderOnTheWay = markOrderOnTheWay;
        this.markOrderDelivered = markOrderDelivered;
        this.changeOrderStatus = changeOrderStatus;
        this.currentUserService = currentUserService;
        this.getOrderDetailUseCase = getOrderDetailUseCase;
    }

    @Override
    public List<ReadyOrder> findReadyForDelivery() {
        return findReadyForDelivery.execute(null);
    }

    @Override
    public UUID customerOf(UUID orderId) {
        return getOrderDetailUseCase.toCustomerId(orderId);
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

    @Override
    public void cancel(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void release(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented yet");
        }
    private void changeStatus(UUID orderId, OrderStatus status, UUID changedById, String reason) {
        try {
            changeOrderStatus.execute(new ChangeOrderStatusCommand(orderId, status, changedById, reason));
        } catch (InvalidStateTransitionException exception) {
            throw new ConflictException(exception.getMessage());
        }
    }
}
