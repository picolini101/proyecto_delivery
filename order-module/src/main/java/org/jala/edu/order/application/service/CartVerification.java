package org.jala.edu.order.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartVerification(List<Line> lines,List<Issue> issues, BigDecimal subtotal) {

    /** Tipo de problema que puede tener un producto del carrito. */
    public enum IssueType {
        /** El catalogo ya no conoce el producto. */
        NO_LONGER_EXISTS,

        /** El producto, existe, pero no esta disponible (sin stock) */
        NOT_AVAILABLE
    }

    /**
     * Una linea verificada, el producto con el precio vigente del catalogo.
     *
     * @param productId   id del producto
     * @param productName nombre actual del producto
     * @param unitPrice   precio vigente de una unidad
     * @param quantity    cantidad que hay en el carrito
     * @param lineSubtotal precio vigente por cantidad
     */
    public record Line(UUID productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineSubtotal){}

    /**
     * Un problema encontrado en un producto del carrito.
     *
     * @param productId         id del producto
     * @param productName       nombre del producto, o {@code null} si el catalogo ya no lo conoce
     * @param type              que problema tiene
     * @param requestedQuantity cantidad que habia en el carrito
     */
    public record Issue(UUID productId, String productName, IssueType type, int requestedQuantity){}

    /** @return {@code true} en caso si no se encontro ningun problema */
    public boolean isValid(){ return issues.isEmpty();}

}
