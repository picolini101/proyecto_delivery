package org.jala.edu.order.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida (INT-002): define QUE necesita el modulo de pedidos del servicio de pagos,
 * sin decir COMO se consigue. Los casos de uso solo conocen esta interfaz.
 * <p>
 * La clase que la implementa (un adaptador, por ejemplo {@code PaymentServiceAdapter}) es la unica que conoce
 * el contrato {@code PaymentService} del core. Si ese contrato cambia, solo cambia el adaptador.
 */
public interface PaymentGatewayPort {

    /**
     * Pide al servicio de pagos que autorice un cobro por el monto indicado.
     * Nunca lanza excepciones por fallas del servicio: las convierte en {@link PaymentAuthorization.Unavailable}.
     *
     * @param orderId         id del pedido que se esta pagando
     * @param customerId      id del cliente que paga
     * @param paymentMethodId id del metodo de pago elegido
     * @param amount          monto total a autorizar
     * @return aprobado, rechazado o servicio no disponible; nunca es null
     */
    PaymentAuthorization authorize(UUID orderId, UUID customerId, UUID paymentMethodId, BigDecimal amount);
}
