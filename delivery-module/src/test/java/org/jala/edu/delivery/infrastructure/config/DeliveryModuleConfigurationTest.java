package org.jala.edu.delivery.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.GetDeliveryHistoryQuery;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
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
}