package org.jala.edu.order.application.usecase.orders;

import java.util.Optional;
import java.util.function.Supplier;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.in.ChangeOrderStatusCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;

public class ChangeOrderStatusUseCase implements UseCase<ChangeOrderStatusCommand, Void> {

    private final OrderRepositoryPort orderRepository;
    private final OrderStatusHistoryPort statusHistory;
    private final TransactionRunner transactions;

    public ChangeOrderStatusUseCase(OrderRepositoryPort orderRepository,
                                    OrderStatusHistoryPort statusHistory,
                                    TransactionRunner transactions) {
        this.orderRepository = orderRepository;
        this.statusHistory = statusHistory;
        this.transactions = transactions;
    }

    @Override
    public Void execute(ChangeOrderStatusCommand command) {
        return transactions.inTransaction(new Supplier<Void>() {
            @Override
            public Void get() {
                OrderId orderId = OrderId.of(command.orderId());

                Optional<Order> optionalOrder = orderRepository.findById(orderId);
                if (optionalOrder.isEmpty()) {
                    throw new NotFoundException("No se encontró el pedido con ID: " + command.orderId());
                }

                Order order = optionalOrder.get();
                OrderStatus previousStatus = order.getStatus();

                // 1. Aplica la transición en el agregado (valida tabla y motivos)
                order.transitionTo(command.newStatus(), command.reason());

                // 2. Crea el registro de auditoría
                OrderStatusChange change = OrderStatusChange.record(
                        order.getId(),
                        previousStatus,
                        command.newStatus(),
                        command.changedById(),
                        command.reason()
                );

                // 3. Guarda de forma atómica el pedido y el historial
                orderRepository.save(order);
                statusHistory.save(change);

                return null;
            }
        });
    }
}