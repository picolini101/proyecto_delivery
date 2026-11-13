package org.jala.edu.delivery.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.infrastructure.contract.FallbackCurrentUserService;
import org.jala.edu.core.infrastructure.contract.FallbackOrderService;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.GetDeliveryHistoryQuery;
import org.jala.edu.delivery.application.port.in.TomarPedidoCommand;
import org.jala.edu.delivery.application.port.out.CourierAvailabilityPort;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.application.usecase.TomarPedidoUseCase;
import org.jala.edu.delivery.domain.model.DeliveryHistory;
import org.junit.jupiter.api.Test;

class DeliveryModuleConfigurationTest {

    private final DeliveryModuleConfiguration configuration = new DeliveryModuleConfiguration();

    @Test
    void theHistoryStartsWithSampleDeliveriesOfTheSampleCourier() {
        // Arrange
        DeliveryHistoryPort port = configuration.deliveryHistoryPort();

        // Act
        var deliveries = port.findByCourierId(SampleUsers.COURIER);

        // Assert
        assertThat(deliveries).hasSize(4);
        assertThat(deliveries).allSatisfy(delivery -> assertThat(delivery.courierId()).isEqualTo(SampleUsers.COURIER));
    }

    @Test
    void theWiredUseCaseReadsTheSampleHistory() {
        // Arrange
        var useCase = configuration.getDeliveryHistoryUseCase(configuration.deliveryHistoryPort(),
                new FallbackUserService());

        // Act
        DeliveryHistory history = useCase.execute(new GetDeliveryHistoryQuery(SampleUsers.COURIER));

        // Assert
        assertThat(history.deliveries()).hasSize(4);
        assertThat(history.total()).isEqualByComparingTo("46.25");
    }

    @Test
    void theWiredTakeOrderUseCaseRejectsASecondOrderOfTheSameCourier() {
        // Arrange
        FallbackOrderService orders = new FallbackOrderService();
        FallbackUserService users = new FallbackUserService();
        CourierAvailabilityPort availability = configuration.courierAvailabilityPort();
        TomarPedidoUseCase tomarPedido = configuration.tomarPedidoUseCase(orders,
                configuration.validateCourierEligibilityUseCase(users, availability), availability,
                new FallbackCurrentUserService(UserRole.COURIER));
        tomarPedido.execute(new TomarPedidoCommand(orders.findReadyForDelivery().getFirst().orderId()));
        TomarPedidoCommand secondOrder = new TomarPedidoCommand(orders.findReadyForDelivery().getFirst().orderId());

        // Act
        Throwable thrown = catchThrowable(() -> tomarPedido.execute(secondOrder));

        // Assert
        assertThat(thrown).hasMessage("El repartidor no está disponible para tomar pedidos");
    }
}
