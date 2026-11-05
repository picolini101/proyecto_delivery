package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigDecimal;
import java.util.List;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.ListReadyOrdersQuery;
import org.jala.edu.delivery.application.port.in.ReadyOrderSummary;
import org.jala.edu.delivery.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;

class ListReadyOrdersUseCaseTest {

    private final FallbackOrderService orders = new FallbackOrderService();
    private final ListReadyOrdersQuery query = new ListReadyOrdersQuery();
    private final ListReadyOrdersUseCase listReadyOrders = new ListReadyOrdersUseCase(orders, new FallbackUserService(),
            new FallbackCurrentUserService(UserRole.COURIER));

    @Test
    void listsReadyOrdersCopyingAddressAndShippingCostFromTheContract() {
        // Act
        List<ReadyOrderSummary> ready = listReadyOrders.execute(query);

        // Assert
        assertThat(ready).extracting(ReadyOrderSummary::orderId).isSorted();
        assertThat(ready).extracting(ReadyOrderSummary::deliveryAddress)
                .containsExactlyInAnyOrder("Av. Siempre Viva 742", "Calle Falsa 123", "Plaza Principal 1");
        assertThat(ready).allSatisfy(summary -> {
            ReadyOrder source = orders.findReadyForDelivery().stream()
                    .filter(order -> order.orderId().equals(summary.orderId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(summary.deliveryAddress()).isEqualTo(source.deliveryAddress());
            assertThat(summary.shippingCost()).isEqualTo(source.shippingCost());
        });
        assertThat(ready).extracting(ReadyOrderSummary::shippingCost)
                .allMatch(cost -> cost.compareTo(new BigDecimal("10.00")) == 0);
    }

    @Test
    void returnsAnEmptyListWhenNothingIsReady() {
        // Arrange
        orders.findReadyForDelivery()
                .forEach(order -> orders.assign(order.orderId(), SampleUsers.COURIER));

        // Act
        List<ReadyOrderSummary> ready = listReadyOrders.execute(query);

        // Assert
        assertThat(ready).isEmpty();
    }

    @Test
    void rejectsAUserWhoIsNotAnActiveCourier() {
        // Arrange
        ListReadyOrdersUseCase asCustomer = new ListReadyOrdersUseCase(orders, new FallbackUserService(),
                new FallbackCurrentUserService(UserRole.CUSTOMER));

        // Act
        Throwable thrown = catchThrowable(() -> asCustomer.execute(query));

        // Assert
        assertThat(thrown).isInstanceOf(UnauthorizedOperationException.class)
                .hasMessage("User not authorized for this operation");
    }
}
