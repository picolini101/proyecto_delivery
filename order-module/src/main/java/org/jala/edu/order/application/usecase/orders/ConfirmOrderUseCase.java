package org.jala.edu.order.application.usecase.orders;

import java.util.Optional;
import java.util.function.Supplier;
import org.jala.edu.core.application.port.out.TransactionRunner;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.ConfirmOrderCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.OrderStatusChange;

public class ConfirmOrderUseCase implements UseCase<ConfirmOrderCommand, OrderSummary> {

    private final OrderRepositoryPort orderRepository;
    private final OrderStatusHistoryPort statusHistory;
    private final TransactionRunner transactions;

    public ConfirmOrderUseCase(OrderRepositoryPort orderRepository,
                               OrderStatusHistoryPort statusHistory,
                               TransactionRunner transactions) {
        this.orderRepository = orderRepository;
        this.statusHistory = statusHistory;
        this.transactions = transactions;
    }

    @Override
    public OrderSummary execute(final ConfirmOrderCommand command) {
        PaymentAttempt attempt = command.paymentAttempt();

        if (!attempt.isApproved()) {
            throw new DomainException("No se puede confirmar el pedido porque el pago no fue aprobado: " + attempt.message());
        }

        return transactions.inTransaction(new Supplier<OrderSummary>() {
            @Override
            public OrderSummary get() {
                OrderId orderId = OrderId.of(command.orderId());

                Optional<Order> optionalOrder = orderRepository.findById(orderId);
                if (optionalOrder.isEmpty()) {
                    throw new NotFoundException("Pedido no encontrado: " + command.orderId());
                }

                Order order = optionalOrder.get();

                if (!order.getCustomerId().value().equals(command.customerId())) {
                    throw new UnauthorizedCustomerException("El pedido no pertenece al cliente especificado.");
                }

                OrderStatus previousStatus = order.getStatus();

                order.transitionTo(OrderStatus.CONFIRMED, null);

                OrderStatusChange change = OrderStatusChange.record(
                        order.getId(),
                        previousStatus,
                        OrderStatus.CONFIRMED,
                        command.customerId(),
                        "Pago aprobado con exito"
                );

                orderRepository.save(order);
                statusHistory.save(change);

                return OrderSummary.from(order);
            }
        });
    }
}