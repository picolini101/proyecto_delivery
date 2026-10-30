package org.jala.edu.order.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resumen del carrito listo para mostrar en pantalla: las lineas agrupadas por comercio y los totales.
 *
 * Un {@code record} es una forma corta de escribir una clase que solo guarda datos.
 * Java crea solo el constructor y un metodo para leer cada dato, con el mismo nombre del campo.
 * Ejemplo: {@code summary.total()} devuelve el total.
 * Los precios son {@link BigDecimal} (numero exacto para dinero); se suman con {@code add}
 * y se multiplican con {@code multiply}.
 *
 * @param groups   lineas del carrito agrupadas por comercio
 * @param subtotal suma de todos los productos, sin envio
 * @param shipping costo de envio (cero si el carrito esta vacio)
 * @param total    subtotal mas envio
 */
public record CartSummary(List<Group> groups, BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {

    /**
     * Las lineas de un mismo comercio.
     *
     * @param commerceId id del comercio
     * @param lines      productos de ese comercio en el carrito
     */
    public record Group(UUID commerceId, List<Line> lines) {
    }

    /**
     * Una linea del carrito: un producto con su cantidad.
     *
     * @param productId    id del producto (la pantalla lo necesita para los botones "+" y "-")
     * @param productName  nombre del producto
     * @param unitPrice    precio de una unidad
     * @param quantity     cantidad en el carrito
     * @param lineSubtotal precio unitario por cantidad
     */
    public record Line(UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineSubtotal) {
    }
}