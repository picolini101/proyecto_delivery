package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.in.MarkOrderReadyForDeliveryCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public class MarkOrderReadyForDeliveryUseCase implements UseCase<MarkOrderReadyForDeliveryCommand, Void> {

    private final OrderRepositoryPort orderRepository;
    private final ChangeOrderStatusUseCase changeOrderStatus;
    private final CatalogService catalogService;
    private final UserService userService;

    public MarkOrderReadyForDeliveryUseCase(OrderRepositoryPort orderRepository,
                                            ChangeOrderStatusUseCase changeOrderStatus,
                                            CatalogService catalogService,
                                            UserService userService) {
        this.orderRepository = orderRepository;
        this.changeOrderStatus = changeOrderStatus;
        this.catalogService = catalogService;
        this.userService = userService;
    }

    @Override
    public Void execute(MarkOrderReadyForDeliveryCommand command) {
        if (!userService.isActiveWithRole(command.merchantId(), UserRole.MERCHANT)) {
            throw new UnauthorizedCustomerException("El usuario no es un comercio activo.");
        }

        OrderId orderId = OrderId.of(command.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + command.orderId()));
        List<UUID> commerceIds = catalogService.findCommerceIdsByMerchant(command.merchantId());
        if (order.getLines().stream().noneMatch(line -> commerceIds.contains(line.getCommerceId()))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece a este comercio.");
        }

        return changeOrderStatus.execute(new ChangeOrderStatusCommand(
                command.orderId(),
                OrderStatus.READY_FOR_DELIVERY,
                command.changedById(),
                "Merchant marked order ready for delivery"));
    }
}
