package org.jala.edu.order.application.usecase.orders;

import java.util.ArrayList;
import java.util.List;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.port.in.StartCheckoutCommand;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.EmptyCartException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;

public class StartCheckoutUseCase implements UseCase<StartCheckoutCommand, OrderSummary> {

    private final UserService userService;
    private final CartRepositoryPort cartRepository;
    private final CartVerifier cartVerifier;
    private final OrderRepositoryPort orderRepository;

    public StartCheckoutUseCase(
            UserService userService,
            CartRepositoryPort cartRepository,
            CartVerifier cartVerifier,
            OrderRepositoryPort orderRepository
    ) {
        this.userService = userService;
        this.cartRepository = cartRepository;
        this.cartVerifier = cartVerifier;
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderSummary execute(StartCheckoutCommand command) {
        // Precondición 1: El usuario debe ser un CUSTOMER activo en el sistema
        boolean isAuthorized = userService.isActiveWithRole(command.customerId(), UserRole.CUSTOMER);
        if (!isAuthorized) {
            throw new UnauthorizedCustomerException();
        }

        // Precondición 2: El cliente debe tener un carrito y no debe estar vacío
        Cart cart = (Cart) cartRepository.findByCustomerId(command.customerId())
                .orElseThrow(EmptyCartException::new);

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        // Precondición 3: Validar disponibilidad y precios vigentes con el catálogo
        CartVerification verification = cartVerifier.verifyOrThrow(cart);

        // Mapeo a Value Objects de Dominio
        List<OrderLine> orderLines = new ArrayList<>();
        for (CartVerification.Line line : verification.lines()) {
            ProductId productId = ProductId.of(line.productId());
            String productName = line.productName();
            Money unitPrice = Money.of(line.unitPrice());
            // 4. Convertimos el int plano al Value Object Quantity
            Quantity quantity = Quantity.of(line.quantity());
            OrderLine orderLine = OrderLine.of(productId, line.commerceId(), productName, unitPrice, quantity);
            orderLines.add(orderLine);
        }

        // Creación del Agregado en estado CREATED mediante Factory Method
        Order order = Order.create(
                CustomerId.of(command.customerId()),
                orderLines,
                Money.of(verification.subtotal())
        );

        Order savedOrder = orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);

        // Proyección al DTO de respuesta
        return OrderSummary.from(savedOrder);
    }
}
