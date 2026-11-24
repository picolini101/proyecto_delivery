package org.jala.edu.core.infrastructure.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.UUID;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.SampleUsers;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;

class FallbackOrderServiceTest {

    private final FallbackOrderService orderService = new FallbackOrderService();

    @Test
    void startsWithTheSampleReadyOrders() {
        // Arrange (fresh service)

        // Act
        List<ReadyOrder> ready = orderService.findReadyForDelivery();

        // Assert
        assertThat(ready).extracting(ReadyOrder::deliveryAddress).containsExactlyInAnyOrder(
                "Av. Siempre Viva 742",
                "Calle Falsa 123",
                "Plaza Principal 1",
                "Carrera 7 # 45-10",
                "Calle 80 # 12-34",
                "Av. Las Palmas 50",
                "Diagonal 15 # 90-20",
                "Carrera 15 # 100-21");
    }

    @Test
    void anAssignedOrderLeavesTheReadyListAndCannotBeTakenTwice() {
        // Arrange
        UUID orderId = firstReadyOrder();
        orderService.assign(orderId, UUID.randomUUID());

        // Act
        Throwable thrown = catchThrowable(() -> orderService.assign(orderId, UUID.randomUUID()));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class);
        assertThat(orderService.findReadyForDelivery()).extracting(ReadyOrder::orderId).doesNotContain(orderId);
    }

    @Test
    void followsTheDeliveryStepsInOrder() {
        // Arrange
        UUID orderId = firstReadyOrder();

        // Act
        Throwable skipped = catchThrowable(() -> orderService.markDelivered(orderId));
        orderService.assign(orderId, UUID.randomUUID());
        orderService.markOnTheWay(orderId);
        orderService.markDelivered(orderId);

        // Assert
        assertThat(skipped).isInstanceOf(ConflictException.class);
    }

    @Test
    void cancelsAnOrderThatIsOnTheWay() {
        // Arrange
        UUID orderId = orderOnTheWay();

        // Act
        orderService.cancel(orderId);

        // Assert
        assertThat(orderService.findReadyForDelivery()).extracting(ReadyOrder::orderId).doesNotContain(orderId);
        assertThat(catchThrowable(() -> orderService.markDelivered(orderId))).isInstanceOf(ConflictException.class);
    }

    @Test
    void releasesAnOrderThatIsOnTheWaySoAnotherCourierCanTakeIt() {
        // Arrange
        UUID orderId = orderOnTheWay();

        // Act
        orderService.release(orderId);

        // Assert
        assertThat(orderService.findReadyForDelivery()).extracting(ReadyOrder::orderId).contains(orderId);
        orderService.assign(orderId, UUID.randomUUID());
    }

    @Test
    void sampleOrdersBelongToTheSampleCustomer() {
        // Arrange
        UUID orderId = firstReadyOrder();

        // Act
        UUID customerId = orderService.customerOf(orderId);

        // Assert
        assertThat(customerId).isEqualTo(SampleUsers.CUSTOMER);
    }

    @Test
    void throwsWhenTheOrderDoesNotExist() {
        // Arrange
        UUID unknown = UUID.randomUUID();

        // Act
        Throwable thrown = catchThrowable(() -> orderService.markOnTheWay(unknown));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertThat(catchThrowable(() -> orderService.customerOf(unknown))).isInstanceOf(NotFoundException.class);
    }

    private UUID firstReadyOrder() {
        return orderService.findReadyForDelivery().getFirst().orderId();
    }

    private UUID orderOnTheWay() {
        UUID orderId = firstReadyOrder();
        orderService.assign(orderId, UUID.randomUUID());
        orderService.markOnTheWay(orderId);
        return orderId;
    }
}
