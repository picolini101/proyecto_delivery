package org.jala.edu.order.infrastructure.adapter.in.contract;

import org.jala.edu.core.contract.CurrentUser;
import org.jala.edu.core.contract.CurrentUserService;
import org.jala.edu.core.contract.ReadyOrder;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.order.application.port.in.AssignedOrderCommand;
import org.jala.edu.order.application.port.in.MarkOrderCommand;
import org.jala.edu.order.application.usecase.orders.FindReadyForDeliveryUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderAssignedUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderDeliveredUseCase;
import org.jala.edu.order.application.usecase.orders.MarkOrderOnTheWayUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceAdapterTest {
    private final FindReadyForDeliveryUseCase findReady = mock(FindReadyForDeliveryUseCase.class);
    private final MarkOrderAssignedUseCase assign = mock(MarkOrderAssignedUseCase.class);
    private final MarkOrderOnTheWayUseCase onTheWay = mock(MarkOrderOnTheWayUseCase.class);
    private final MarkOrderDeliveredUseCase delivered = mock(MarkOrderDeliveredUseCase.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);

    private OrderServiceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OrderServiceAdapter(findReady, assign, onTheWay, delivered, currentUserService);
    }

    @Test
    void returnsReadyOrdersFromUseCase() {
        List<ReadyOrder> readyOrders = List.of(new ReadyOrder(UUID.randomUUID(), "Street 1", new BigDecimal("5.00")));
        when(findReady.execute(null)).thenReturn(readyOrders);

        assertThat(adapter.findReadyForDelivery()).isEqualTo(readyOrders);
        verify(findReady).execute(null);
    }

    @Test
    void assignsOrderUsingCourierAndCurrentUser() {
        UUID orderId = UUID.randomUUID();
        UUID courierId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(currentUserService.findCurrentUser()).thenReturn(Optional.of(new CurrentUser(actorId, UserRole.ADMIN, "Admin")));

        adapter.assign(orderId, courierId);

        verify(assign).execute(new AssignedOrderCommand(orderId, courierId, actorId));
    }

    @Test
    void marksOrderOnTheWayUsingCurrentUser() {
        UUID orderId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(currentUserService.findCurrentUser()).thenReturn(Optional.of(new CurrentUser(actorId, UserRole.ADMIN, "Admin")));

        adapter.markOnTheWay(orderId);

        verify(onTheWay).execute(new MarkOrderCommand(orderId, actorId));
    }

    @Test
    void marksOrderDeliveredUsingCurrentUser() {
        UUID orderId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(currentUserService.findCurrentUser()).thenReturn(Optional.of(new CurrentUser(actorId, UserRole.ADMIN, "Admin")));

        adapter.markDelivered(orderId);

        verify(delivered).execute(new MarkOrderCommand(orderId, actorId));
    }
}
