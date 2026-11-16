package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jala.edu.delivery.DeliveryFixtures.delivery;
import static org.jala.edu.delivery.DeliveryFixtures.deliveryOf;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.infrastructure.contract.FallbackUserService;
import org.jala.edu.delivery.application.port.in.GetDeliveryHistoryQuery;
import org.jala.edu.delivery.application.port.out.DeliveryHistoryPort;
import org.jala.edu.delivery.domain.exception.NotAuthorizedException;
import org.jala.edu.delivery.domain.model.CompletedDelivery;
import org.jala.edu.delivery.domain.model.DeliveryHistory;
import org.jala.edu.delivery.domain.model.DeliverySortOrder;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryDeliveryHistoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GetDeliveryHistoryUseCaseTest {

    private final InMemoryDeliveryHistoryAdapter repository = new InMemoryDeliveryHistoryAdapter();
    private final GetDeliveryHistoryUseCase getHistory =
            new GetDeliveryHistoryUseCase(repository, new FallbackUserService());

    @Test
    void returnsTheCouriersDeliveriesMostRecentFirstWithTheirTotal() {
        // Arrange
        CompletedDelivery oldest = delivery("10.00", "2026-10-20");
        CompletedDelivery newest = delivery("15.75", "2026-10-28");
        CompletedDelivery middle = delivery("12.50", "2026-10-22");
        repository.add(oldest);
        repository.add(newest);
        repository.add(middle);

        // Act
        DeliveryHistory history = getHistory.execute(new GetDeliveryHistoryQuery(SampleUsers.COURIER));

        // Assert
        assertThat(history.deliveries()).containsExactly(newest, middle, oldest);
        assertThat(history.total()).isEqualByComparingTo("38.25");
    }

    @Test
    void returnsTheHistoryInTheRequestedOrder() {
        // Arrange
        CompletedDelivery cheap = delivery("8.00", "2026-10-28");
        CompletedDelivery expensive = delivery("15.75", "2026-10-20");
        CompletedDelivery middle = delivery("12.50", "2026-10-22");
        repository.add(cheap);
        repository.add(expensive);
        repository.add(middle);

        // Act
        DeliveryHistory history = getHistory.execute(
                new GetDeliveryHistoryQuery(SampleUsers.COURIER, DeliverySortOrder.HIGHEST_AMOUNT));

        // Assert
        assertThat(history.deliveries()).containsExactly(expensive, middle, cheap);
        assertThat(history.total()).isEqualByComparingTo("36.25");
    }

    @Test
    void usesTheMostRecentOrderWhenNoneIsRequested() {
        // Arrange
        CompletedDelivery oldest = delivery("10.00", "2026-10-20");
        CompletedDelivery newest = delivery("15.75", "2026-10-28");
        repository.add(oldest);
        repository.add(newest);

        // Act
        DeliveryHistory history = getHistory.execute(new GetDeliveryHistoryQuery(SampleUsers.COURIER, null));

        // Assert
        assertThat(history.deliveries()).containsExactly(newest, oldest);
    }

    @Test
    void doesNotIncludeTheDeliveriesOfOtherCouriers() {
        // Arrange
        CompletedDelivery mine = delivery("10.00", "2026-10-20");
        repository.add(mine);
        repository.add(deliveryOf(UUID.randomUUID(), "99.00", "2026-10-21"));

        // Act
        DeliveryHistory history = getHistory.execute(new GetDeliveryHistoryQuery(SampleUsers.COURIER));

        // Assert
        assertThat(history.deliveries()).containsExactly(mine);
        assertThat(history.total()).isEqualByComparingTo("10.00");
    }

    @Test
    void aCourierWithoutFinishedDeliveriesGetsAnEmptyHistory() {
        // Arrange
        GetDeliveryHistoryQuery query = new GetDeliveryHistoryQuery(SampleUsers.COURIER);

        // Act
        DeliveryHistory history = getHistory.execute(query);

        // Assert
        assertThat(history.isEmpty()).isTrue();
        assertThat(history.total()).isZero();
    }

    static List<UUID> usersThatAreNotCouriers() {
        return List.of(SampleUsers.ADMIN, SampleUsers.MERCHANT, SampleUsers.CUSTOMER, SampleUsers.INACTIVE_CUSTOMER,
                UUID.randomUUID());
    }

    @ParameterizedTest
    @MethodSource("usersThatAreNotCouriers")
    void rejectsAUserThatIsNotAnActiveCourierWithoutReadingTheHistory(UUID userId) {
        // Arrange
        DeliveryHistoryPort mustNotBeRead = courierId -> {
            throw new AssertionError("The history must not be read for an unauthorized user");
        };
        GetDeliveryHistoryUseCase useCase = new GetDeliveryHistoryUseCase(mustNotBeRead, new FallbackUserService());

        // Act
        Throwable thrown = catchThrowable(() -> useCase.execute(new GetDeliveryHistoryQuery(userId)));

        // Assert
        assertThat(thrown).isInstanceOf(NotAuthorizedException.class).hasMessageContaining(userId.toString());
    }

    @Test
    void failsWhenTheHistoryCannotBeRead() {
        // Arrange
        DeliveryHistoryPort broken = courierId -> {
            throw new IllegalStateException("history unavailable");
        };
        GetDeliveryHistoryUseCase useCase = new GetDeliveryHistoryUseCase(broken, new FallbackUserService());

        // Act
        Throwable thrown = catchThrowable(() -> useCase.execute(new GetDeliveryHistoryQuery(SampleUsers.COURIER)));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage("history unavailable");
    }
}
