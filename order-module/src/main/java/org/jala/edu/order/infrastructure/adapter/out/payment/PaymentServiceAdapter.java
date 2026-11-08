package org.jala.edu.order.infrastructure.adapter.out.payment;

import java.math.BigDecimal;
import java.util.UUID;

import org.jala.edu.core.contract.PaymentResult;
import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.order.application.port.out.PaymentAuthorization;
import org.jala.edu.order.application.port.out.PaymentGatewayPort;

/**
 * Adaptador (patron Adapter) que conecta el puerto {@link PaymentGatewayPort} del modulo de pedidos con el
 * contrato {@link PaymentService} del core, que implementa el modulo de pagos.
 * <p>
 * Hace dos traducciones:
 * <ul>
 *   <li>{@link PaymentResult} (aprobado si/no) a {@link PaymentAuthorization} (aprobado, rechazado o no disponible).</li>
 *   <li>Cualquier excepcion o respuesta vacia del servicio a {@link PaymentAuthorization.Unavailable}, para que el
 *       caso de uso nunca dependa de como falla el otro modulo.</li>
 * </ul>
 * No tiene reglas de negocio: solo traduce. Se registra como bean en {@code OrderModuleConfiguration}.
 */
public class PaymentServiceAdapter implements PaymentGatewayPort {

    /** Mensaje cuando el servicio de pagos no responde o falla. */
    static final String SERVICE_UNAVAILABLE_MESSAGE =
            "El servicio de pagos no esta disponible. Intenta de nuevo mas tarde.";

    /** Mensaje cuando el pago se rechaza y el servicio no dice el motivo. */
    static final String DEFAULT_REJECTION_MESSAGE = "El pago fue rechazado.";

    /** Contrato del core que atiende los pagos. */
    private final PaymentService paymentService;

    /**
     * Recibe el servicio de pagos. Spring lo entrega desde {@code OrderModuleConfiguration}.
     *
     * @param paymentService servicio de pagos del core
     */
    public PaymentServiceAdapter(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Envia la solicitud de autorizacion e interpreta la respuesta.
     *
     * @param orderId         id del pedido que se esta pagando
     * @param customerId      id del cliente que paga
     * @param paymentMethodId id del metodo de pago
     * @param amount          monto total a autorizar
     * @return aprobado, rechazado (con su motivo) o no disponible si el servicio fallo
     */
    @Override
    public PaymentAuthorization authorize(UUID orderId, UUID customerId, UUID paymentMethodId, BigDecimal amount) {
        PaymentResult result;
        try {
            result = paymentService.pay(orderId, customerId, paymentMethodId, amount);
        } catch (RuntimeException e) {
            // El servicio no respondio o devolvio un error: no sabemos si cobro, asi que no se aprueba.
            return new PaymentAuthorization.Unavailable(SERVICE_UNAVAILABLE_MESSAGE);
        }
        if (result == null) {
            return new PaymentAuthorization.Unavailable(SERVICE_UNAVAILABLE_MESSAGE);
        }
        if (result.approved()) {
            return new PaymentAuthorization.Approved(result.paymentReference());
        }
        String reason = result.reason();
        if (reason == null || reason.isBlank()) {
            reason = DEFAULT_REJECTION_MESSAGE;
        }
        return new PaymentAuthorization.Rejected(result.paymentReference(), reason);
    }
}
