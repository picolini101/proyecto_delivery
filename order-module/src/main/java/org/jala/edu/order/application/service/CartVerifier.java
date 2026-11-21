package org.jala.edu.order.application.service;


import org.jala.edu.order.application.port.out.CatalogPort;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.order.domain.exception.ProductUnavailableException;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CartItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Clase clave para verificar un carrito
 * Cualquier caso de uso que tenga un {@link Cart} la puede usar (por ejemplo la finalizacion de compra),
 * sin repetir la logica de consultar el catalogo.
 * <p>
 * En este caso es solo lectura, nunca modifica el carrito. Si hay problemas, quien la usa decide que hacer
 * <p>
 *
 */

public class CartVerifier {

    /** Para consultar el precio vigente y la disponibilidad de cada producto. */
    private final CatalogPort catalog;

    /**
     * Recibe el catalogo. Spring lo entrega desde {@code OrderModuleConfiguration}.
     *
     * @param catalog servicio del catalogo
     */
    public CartVerifier(CatalogPort catalog) {
        this.catalog = catalog;
    }

    /**
     * Verifica cada producto del carrito y devuelve TODOS los problemas encontrados, no solo el primero.
     * Para cada producto:
     * <ol>
     *   <li>Busca el producto en el catalogo. Si no existe: problema NO_LONGER_EXISTS y sigue con el siguiente.</li>
     *   <li>Calcula la linea con el precio VIGENTE (precio del catalogo x cantidad del carrito).</li>
     *   <li>Pregunta si hay disponibilidad para esa cantidad. Si no hay: problema NOT_AVAILABLE.</li>
     * </ol>
     *
     * @param cart carrito a verificar
     * @return lineas con precio vigente, problemas y subtotal
     */
    public CartVerification verify(Cart cart){
        List<CartVerification.Line> lines = new ArrayList<>();
        List<CartVerification.Issue> issues = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for(CartItem item: cart.getItems()){

            // orElse(null): saca el producto del Optional, o null si el catalogo ya no lo conoce
            ProductInfo product = catalog.findProduct(item.getProductId()).orElse(null);

            if(product == null){
                issues.add(new CartVerification.Issue(item.getProductId(),null,
                        CartVerification.IssueType.NO_LONGER_EXISTS, item.getQuantity()));
                continue;
            }

            // Precio vigente: el que devuelve el catalogo ahora, no uno guardado antes
            BigDecimal lineSubtotal = product.price().multiply(BigDecimal.valueOf(item.getQuantity()));
            lines.add(new CartVerification.Line(item.getProductId(), item.getCommerceId(), product.name(), product.price(),
                    item.getQuantity(), lineSubtotal));
            subtotal = subtotal.add(lineSubtotal);

            if (!catalog.isAvailable(item.getProductId(), item.getQuantity())) {
                issues.add(new CartVerification.Issue(item.getProductId(), product.name(),
                        CartVerification.IssueType.NOT_AVAILABLE, item.getQuantity()));
            }
        }

        return new CartVerification(lines, issues, subtotal);
    }

    /**
     * Igual que {@link #verify(Cart)}, pero si hay problemas lanza {@link ProductUnavailableException}
     * con un mensaje que dice cuales productos cambiaron. El carrito queda intacto.
     * Pensado para detener la creacion del pedido.
     *
     * @param cart carrito a verificar (no se modifica)
     * @return la verificacion, que en este caso siempre esta libre de problemas
     * @throws ProductUnavailableException si algun producto ya no existe o ya no esta disponible
     */
    public CartVerification verifyOrThrow(Cart cart) {
        CartVerification verification = verify(cart);
        if (!verification.isValid()) {
            throw new ProductUnavailableException(buildMessage(verification.issues()), productIdsOf(verification));
        }
        return verification;
    }

    /**
     * Arma el mensaje para el usuario, por ejemplo:
     * "Cambio la disponibilidad de estos productos: Empanada (cantidad pedida: 11). Ajusta tu carrito para continuar."
     *
     * @param issues problemas encontrados
     * @return el texto del mensaje
     */
    private String buildMessage(List<CartVerification.Issue> issues) {
        List<String> parts = new ArrayList<>();
        for (CartVerification.Issue issue : issues) {
            if (issue.type() == CartVerification.IssueType.NO_LONGER_EXISTS) {
                parts.add("producto " + issue.productId() + " ya no existe");
            } else {
                parts.add(issue.productName() + " (cantidad pedida: " + issue.requestedQuantity() + ")");
            }
        }
        return "Cambio la disponibilidad de estos productos: " + String.join("; ", parts)
                + ". Ajusta tu carrito para continuar.";
    }


    /**
     * @param verification resultado con problemas
     * @return los ids de los productos con problemas
     */

    private List<UUID> productIdsOf(CartVerification verification) {
        List<UUID> ids = new ArrayList<>();
        for (CartVerification.Issue issue : verification.issues()) {
            ids.add(issue.productId());
        }
        return ids;
    }
}

