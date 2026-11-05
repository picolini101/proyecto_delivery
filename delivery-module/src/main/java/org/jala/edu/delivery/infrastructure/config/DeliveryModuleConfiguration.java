package org.jala.edu.delivery.infrastructure.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.application.usecase.GetDeliveryHistoryUseCase;
import org.jala.edu.delivery.application.usecase.ListCouriersUseCase;
import org.jala.edu.delivery.application.usecase.ListReadyOrdersUseCase;
import org.jala.edu.delivery.application.usecase.RegisterCourierUseCase;
import org.jala.edu.delivery.application.usecase.TomarPedidoUseCase;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryDeliveryHistoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Register the use cases and adapters for the Delivery module.
 */
@Configuration
public class DeliveryModuleConfiguration {

    @Bean
    public RegisterCourierUseCase registerCourierUseCase(
            CourierRepositoryPort couriers,
            UserService users) {

        return new RegisterCourierUseCase(couriers, users);
    }

    @Bean
    public ListCouriersUseCase listCouriersUseCase(
            CourierRepositoryPort couriers) {

        return new ListCouriersUseCase(couriers);
    }

    /**
     * The history only has an in-memory adapter, so there is nothing to switch between. It starts with a few sample
     * deliveries of the sample courier until deliveries can be finished from the application.
     */
    @Bean
    public DeliveryHistoryPort deliveryHistoryPort() {
        InMemoryDeliveryHistoryAdapter history = new InMemoryDeliveryHistoryAdapter();
        history.add(sampleDelivery("00000000-0000-0000-0000-000000000101", "10.00", "Centro", LocalDate.of(2026, 10, 20)));
        history.add(sampleDelivery("00000000-0000-0000-0000-000000000102", "12.50", "Villa Fatima", LocalDate.of(2026, 10, 22)));
        history.add(sampleDelivery("00000000-0000-0000-0000-000000000103", "8.00", "Sopocachi", LocalDate.of(2026, 10, 25)));
        history.add(sampleDelivery("00000000-0000-0000-0000-000000000104", "15.75", "Miraflores", LocalDate.of(2026, 10, 28)));
        return history;
    }

    @Bean
    public GetDeliveryHistoryUseCase getDeliveryHistoryUseCase(DeliveryHistoryPort deliveryHistoryPort,
                                                               UserService userService) {
        return new GetDeliveryHistoryUseCase(deliveryHistoryPort, userService);
    }

    @Bean
    public ListReadyOrdersUseCase listReadyOrdersUseCase(OrderService orderService, UserService userService,
                                                          CurrentUserService currentUserService) {
        return new ListReadyOrdersUseCase(orderService, userService, currentUserService);
    }

    @Bean
    public TomarPedidoUseCase tomarPedidoUseCase(OrderService orderService, UserService userService,
                                                  CurrentUserService currentUserService) {
        return new TomarPedidoUseCase(orderService, userService, currentUserService);
    }

    private static CompletedDelivery sampleDelivery(String orderId, String amount, String locality, LocalDate deliveredOn) {
        return new CompletedDelivery(UUID.fromString(orderId), SampleUsers.COURIER, new BigDecimal(amount), locality,
                deliveredOn);
    }
}
