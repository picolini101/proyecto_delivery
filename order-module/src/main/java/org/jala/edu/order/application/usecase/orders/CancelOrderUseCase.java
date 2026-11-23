package org.jala.edu.order.application.usecase.orders;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.in.CancelOrderCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;

public class CancelOrderUseCase implements UseCase<CancelOrderCommand, Void> {

    private final OrderRepositoryPort orderRepository;
    private final OrderStatusHistoryPort statusHistory;
    private final TransactionRunner transactions;

    public CancelOrderUseCase(OrderRepositoryPort orderRepository,
                              OrderStatusHistoryPort statusHistory,
                              TransactionRunner transactions) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository es obligatorio");
        this.statusHistory = Objects.requireNonNull(statusHistory, "statusHistory es obligatorio");
        this.transactions = Objects.requireNonNull(transactions, "transactions es obligatorio");
    }

    @Override
    public Void execute(CancelOrderCommand command) {
        Objects.requireNonNull(command, "command es obligatorio");

        if (command.reason() == null || command.reason().trim().isEmpty()) {
            throw new DomainException("El motivo de cancelación es obligatorio.");
        }

        return transactions.inTransaction(new Supplier<Void>() {
            @Override
            public Void get() {
                OrderId orderId = OrderId.of(command.orderId());

                Optional<Order> optionalOrder = orderRepository.findById(orderId);
                if (optionalOrder.isEmpty()) {
                    throw new NotFoundException("Pedido no encontrado con ID: " + command.orderId());
                }

                Order order = optionalOrder.get();

                if (!order.getCustomerId().value().equals(command.customerId())) {
                    throw new UnauthorizedCustomerException("El pedido no pertenece al cliente solicitante.");
                }

                OrderStatus previousStatus = order.getStatus();
                String cleanReason = command.reason().trim();

                order.transitionTo(OrderStatus.CANCELLED, cleanReason);

                OrderStatusChange change = OrderStatusChange.record(
                        order.getId(),
                        previousStatus,
                        OrderStatus.CANCELLED,
                        command.customerId(),
                        cleanReason
                );

                orderRepository.save(order);
                statusHistory.save(change);

                return null;
            }
        });
    }
}