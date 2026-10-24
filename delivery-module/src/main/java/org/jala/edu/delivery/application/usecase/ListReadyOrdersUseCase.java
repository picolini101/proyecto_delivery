package org.jala.edu.delivery.application.usecase;

import java.util.Comparator;
import java.util.List;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.ListReadyOrdersQuery;
import org.jala.edu.delivery.application.port.in.ReadyOrderSummary;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;

/**
 * Lists the orders {@link OrderService} reports as ready for delivery, for an active courier.
 */
public class ListReadyOrdersUseCase implements UseCase<ListReadyOrdersQuery, List<ReadyOrderSummary>> {

    private final OrderService orderService;
    private final UserService userService;

    public ListReadyOrdersUseCase(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @Override
    public List<ReadyOrderSummary> execute(ListReadyOrdersQuery query) {
        if (!userService.isActiveWithRole(query.courierId(), UserRole.COURIER)) {
            throw new UnauthorizedOperationException();
        }
        return orderService.findReadyForDelivery().stream()
                .map(ReadyOrderSummary::copyOf)
                .sorted(Comparator.comparing(ReadyOrderSummary::orderId))
                .toList();
    }
}
