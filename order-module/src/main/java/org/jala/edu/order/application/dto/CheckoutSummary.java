package org.jala.edu.order.application.dto;

import java.math.BigDecimal;
import java.util.List;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.domain.model.Cart;

/**
 * DTO que representa el resumen final de la compra antes de proceder al pago.
 * Contiene el desglose de productos con precios vigentes, los subtotales,
 * el costo de envío fijo y el total definitivo a cobrar.
 *
 * @param lines        Líneas de productos verificados con su precio unitario y subtotal.
 * @param subtotal     Suma de los subtotales de todos los productos, sin incluir envío.
 * @param shippingCost Costo de envío fijo (D-05). Es cero si el carrito está vacío.
 * @param total        Monto definitivo a pagar (subtotal + shippingCost).
 * @param isValid      Bandera que indica si el carrito pasó las validaciones de stock y existencia.
 * @param issues       Lista de problemas encontrados durante la verificación (ej. falta de stock).
 */

public record CheckoutSummary(List<CartVerification.Line> lines, BigDecimal subtotal, BigDecimal shippingCost, BigDecimal total,
        boolean isValid,
        List<CartVerification.Issue> issues) {

    /**
     * Crea una instancia de CheckoutSummary a partir del resultado de la verificación del carrito.
     * Aplica la regla de negocio D-05 para calcular el envío fijo si hay productos.
     *
     * @param verification El resultado de {@link org.jala.edu.order.application.service.CartVerifier}.
     * @return Un objeto CheckoutSummary preparado para ser consumido por la interfaz de usuario.
     */
    public static CheckoutSummary from(CartVerification verification) {
        BigDecimal shipping = verification.lines().isEmpty() ? BigDecimal.ZERO : Cart.SHIPPING_COST;
        BigDecimal total = verification.subtotal().add(shipping);

        return new CheckoutSummary(verification.lines(), verification.subtotal(), shipping, total,
                verification.isValid(), verification.issues());
    }
}