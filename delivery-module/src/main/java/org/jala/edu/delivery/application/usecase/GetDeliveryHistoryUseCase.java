package org.jala.edu.delivery.application.usecase;

import java.util.Comparator;
import java.util.List;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.in.GetDeliveryHistoryQuery;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.domain.exception.NotAuthorizedException;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.jala.edu.delivery.domain.model.DeliveryHistory;

public class GetDeliveryHistoryUseCase implements UseCase<GetDeliveryHistoryQuery, DeliveryHistory> {

    private final DeliveryHistoryPort deliveryHistory;
    private final UserService userService;

    public GetDeliveryHistoryUseCase(DeliveryHistoryPort deliveryHistory, UserService userService) {
        this.deliveryHistory = deliveryHistory;
        this.userService = userService;
    }

    /**
     * Returns the courier's finished deliveries, most recent first. A courier without deliveries gets an empty history.
     *
     * @throws NotAuthorizedException if the user is not an active courier
     */
    @Override
    public DeliveryHistory execute(GetDeliveryHistoryQuery query) {
        if (!userService.isActiveWithRole(query.courierId(), UserRole.COURIER)) {
            throw new NotAuthorizedException(query.courierId());
        }
        List<CompletedDelivery> deliveries = deliveryHistory.findByCourierId(query.courierId()).stream()
                .sorted(Comparator.comparing(CompletedDelivery::deliveredOn).reversed())
                .toList();
        return new DeliveryHistory(deliveries);
    }
}
