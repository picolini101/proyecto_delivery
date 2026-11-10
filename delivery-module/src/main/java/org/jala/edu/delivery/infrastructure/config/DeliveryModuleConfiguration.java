package org.jala.edu.delivery.infrastructure.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.OrderService;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.delivery.application.port.out.ActiveDeliveryPort;
import org.jala.edu.delivery.application.port.out.ConfirmationCodeGenerator;
import org.jala.edu.delivery.application.port.out.CourierAvailabilityPort;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.application.usecase.CancelDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.ConfirmDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.DelegateDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.FindActiveDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.FindCustomerShipmentUseCase;
import org.jala.edu.delivery.application.usecase.GetDeliveryHistoryUseCase;
import org.jala.edu.delivery.application.usecase.ListCouriersUseCase;
import org.jala.edu.delivery.application.usecase.ListReadyOrdersUseCase;
import org.jala.edu.delivery.application.usecase.PickUpDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.RegisterCourierUseCase;
import org.jala.edu.delivery.application.usecase.StartDeliveryUseCase;
import org.jala.edu.delivery.application.usecase.TomarPedidoUseCase;
import org.jala.edu.delivery.application.usecase.ValidateCourierEligibilityUseCase;
import org.jala.edu.delivery.infrastructure.adapter.out.code.RandomConfirmationCodeGenerator;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryActiveDeliveryAdapter;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierAvailabilityAdapter;
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

    /**
     * Availability only has an in-memory adapter, so there is nothing to switch between. Every courier starts available.
     */
    @Bean
    public CourierAvailabilityPort courierAvailabilityPort() {
        return new InMemoryCourierAvailabilityAdapter();
    }

    @Bean
    public ValidateCourierEligibilityUseCase validateCourierEligibilityUseCase(UserService userService,
                                                                               CourierAvailabilityPort availability) {
        return new ValidateCourierEligibilityUseCase(userService, availability);
    }

    @Bean
    public TomarPedidoUseCase tomarPedidoUseCase(OrderService orderService,
                                                  ValidateCourierEligibilityUseCase validateEligibility,
                                                  CourierAvailabilityPort availability,
                                                  CurrentUserService currentUserService) {
        return new TomarPedidoUseCase(orderService, validateEligibility, availability, currentUserService);
    }

    @Bean
    public ActiveDeliveryPort activeDeliveryPort() {
        return new InMemoryActiveDeliveryAdapter();
    }

    @Bean
    public ConfirmationCodeGenerator confirmationCodeGenerator() {
        return new RandomConfirmationCodeGenerator();
    }

    @Bean
    public StartDeliveryUseCase startDeliveryUseCase(OrderService orderService, UserService userService,
                                                      CurrentUserService currentUserService,
                                                      ActiveDeliveryPort activeDeliveries,
                                                      ConfirmationCodeGenerator confirmationCodes) {
        return new StartDeliveryUseCase(orderService, userService, currentUserService, activeDeliveries,
                confirmationCodes);
    }

    @Bean
    public PickUpDeliveryUseCase pickUpDeliveryUseCase(UserService userService,
                                                        CurrentUserService currentUserService,
                                                        ActiveDeliveryPort activeDeliveries) {
        return new PickUpDeliveryUseCase(userService, currentUserService, activeDeliveries);
    }

    @Bean
    public ConfirmDeliveryUseCase confirmDeliveryUseCase(OrderService orderService, UserService userService,
                                                          CurrentUserService currentUserService,
                                                          ActiveDeliveryPort activeDeliveries) {
        return new ConfirmDeliveryUseCase(orderService, userService, currentUserService, activeDeliveries);
    }

    @Bean
    public CancelDeliveryUseCase cancelDeliveryUseCase(OrderService orderService, UserService userService,
                                                        CurrentUserService currentUserService,
                                                        ActiveDeliveryPort activeDeliveries) {
        return new CancelDeliveryUseCase(orderService, userService, currentUserService, activeDeliveries);
    }

    @Bean
    public DelegateDeliveryUseCase delegateDeliveryUseCase(OrderService orderService, UserService userService,
                                                            CurrentUserService currentUserService,
                                                            ActiveDeliveryPort activeDeliveries) {
        return new DelegateDeliveryUseCase(orderService, userService, currentUserService, activeDeliveries);
    }

    @Bean
    public FindActiveDeliveryUseCase findActiveDeliveryUseCase(UserService userService,
                                                                CurrentUserService currentUserService,
                                                                ActiveDeliveryPort activeDeliveries) {
        return new FindActiveDeliveryUseCase(userService, currentUserService, activeDeliveries);
    }

    @Bean
    public FindCustomerShipmentUseCase findCustomerShipmentUseCase(UserService userService,
                                                                    CurrentUserService currentUserService,
                                                                    ActiveDeliveryPort activeDeliveries) {
        return new FindCustomerShipmentUseCase(userService, currentUserService, activeDeliveries);
    }

    private static CompletedDelivery sampleDelivery(String orderId, String amount, String locality, LocalDate deliveredOn) {
        return new CompletedDelivery(UUID.fromString(orderId), SampleUsers.COURIER, new BigDecimal(amount), locality,
                deliveredOn);
    }
}
