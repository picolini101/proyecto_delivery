package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderStatus;

import java.util.List;
import java.util.UUID;

public class ListConfirmedOrdersByMerchantUseCase implements UseCase<UUID, List<Order>> {

    private final OrderRepositoryPort orderRepository;
    private final UserService userService;
    private final CatalogService catalogService;

    public ListConfirmedOrdersByMerchantUseCase(OrderRepositoryPort orderRepository, UserService userService,
                                                CatalogService catalogService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.catalogService = catalogService;
    }

    @Override
    public List<Order> execute(UUID merchantId) {
        if (!userService.isActiveWithRole(merchantId, UserRole.MERCHANT)) {
            throw new UnauthorizedCustomerException("El usuario no es un comercio activo.");
        }
        List<UUID> commerceIds = catalogService.findCommerceIdsByMerchant(merchantId);

        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.CONFIRMED)
                .filter(order -> order.getLines().stream()
                        .anyMatch(line -> commerceIds.contains(line.getCommerceId())))
                .toList();
    }
}
