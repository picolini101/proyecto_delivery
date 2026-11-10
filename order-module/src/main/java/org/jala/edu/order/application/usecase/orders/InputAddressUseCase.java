package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.port.in.InputAddressCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;

public class InputAddressUseCase implements UseCase<InputAddressCommand, OrderSummary> {
    private final OrderRepositoryPort orders;

    public InputAddressUseCase(OrderRepositoryPort orders) {
        this.orders = orders;
    }

    @Override
    public OrderSummary execute(InputAddressCommand command) {
        DeliveryAddress address = new DeliveryAddress(command.city(), command.avenue(), command.houseNumber());
        Order order = orders.findById(OrderId.of(command.orderId())).orElseThrow(OrderNotFoundException::new);
        if (!order.getCustomerId().value().equals(command.customerId())) {
            throw new UnauthorizedCustomerException("El usuario no es propietario del pedido.");
        }
        order.inputDeliveryAddress(address);
        return OrderSummary.from(orders.save(order));
    }
}
