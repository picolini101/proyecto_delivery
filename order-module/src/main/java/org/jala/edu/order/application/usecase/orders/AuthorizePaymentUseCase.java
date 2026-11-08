package org.jala.edu.order.application.usecase.orders;

import java.math.BigDecimal;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.dto.PaymentAttempt;
import org.jala.edu.order.application.port.in.AuthorizePaymentCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.application.port.out.PaymentGatewayPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderStatus;

/**
 * Caso de uso HU-10 (CU-02, INT-002): autorizar el pago de un pedido por su monto total.
 * <p>
 * Este caso de uso SOLO LEE el pedido: no cambia su estado, no toca el carrito y no crea nada.
 * Confirmar el pedido (HU-11) o cancelarlo por un pago fallido (HU-12) son trabajo de otras HU, que deben
 * decidir segun {@link PaymentAttempt#isApproved()}.
 * <p>
 * Un rechazo del servicio de pagos (caso A4) o una falla del servicio NO lanzan excepcion: llegan dentro del
 * resultado, para que la pantalla muestre el motivo al cliente.
 */
public class AuthorizePaymentUseCase implements UseCase<AuthorizePaymentCommand, PaymentAttempt> {

    /** Para validar que el cliente existe, esta activo y tiene rol CUSTOMER. */
    private final UserService userService;

    /** Para leer el pedido que se va a pagar. */
    private final OrderRepositoryPort orderRepository;

    /** Puerto de salida hacia el servicio de pagos (INT-002). */
    private final PaymentGatewayPort paymentGateway;

    /**
     * Recibe las dependencias que necesita. Spring las entrega desde {@code OrderModuleConfiguration}.
     *
     * @param userService     servicio de usuarios
     * @param orderRepository repositorio de pedidos
     * @param paymentGateway  puerto hacia el servicio de pagos
     */
    public AuthorizePaymentUseCase(UserService userService, OrderRepositoryPort orderRepository,
                                   PaymentGatewayPort paymentGateway) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Pide al servicio de pagos que autorice el monto total del pedido. Pasos:
     * <ol>
     *   <li>Valida que el cliente este activo y sea CUSTOMER.</li>
     *   <li>Busca el pedido y revisa que sea del cliente y que siga pendiente de pago (estado CREATED).</li>
     *   <li>Calcula el monto total del pedido.</li>
     *   <li>Envia la solicitud de autorizacion al servicio de pagos.</li>
     *   <li>Devuelve el resultado (aprobado, rechazado o no disponible).</li>
     * </ol>
     *
     * @param command pedido, cliente y metodo de pago
     * @return el intento de pago con el monto y la respuesta del servicio
     * @throws UnauthorizedCustomerException si el usuario no es un cliente activo o el pedido no es suyo
     * @throws NotFoundException             si el pedido no existe
     * @throws ConflictException             si el pedido ya no esta pendiente de pago
     */
    @Override
    public PaymentAttempt execute(AuthorizePaymentCommand command) {
        // 1. Validar al cliente
        if (!userService.isActiveWithRole(command.customerId(), UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }

        // 2. Buscar el pedido: debe existir, ser del cliente y estar pendiente de pago
        Order order = orderRepository.findById(OrderId.of(command.orderId()))
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (!order.getCustomerId().equals(CustomerId.of(command.customerId()))) {
            throw new UnauthorizedCustomerException("El pedido no pertenece a este cliente.");
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ConflictException("El pedido ya no esta pendiente de pago.");
        }

        // 3. Monto total y 4. solicitud al servicio de pagos
        BigDecimal amount = totalOf(order);
        PaymentAuthorization authorization = paymentGateway.authorize(
                command.orderId(), command.customerId(), command.paymentMethodId(), amount);

        // 5. Devolver el resultado sin cambiar nada
        return new PaymentAttempt(command.orderId(), amount, authorization);
    }

    /**
     * Monto total del pedido. Si el pedido ya tiene su total definitivo (HU-09) se usa ese; mientras no exista,
     * el total es el subtotal mas el envio fijo del carrito.
     *
     * @param order pedido a pagar
     * @return monto total a autorizar
     */
    private BigDecimal totalOf(Order order) {
        Money total = order.getTotal();
        if (total != null) {
            return total.value();
        }
        return order.getSubtotal().add(Money.of(Cart.SHIPPING_COST)).value();
    }
}
