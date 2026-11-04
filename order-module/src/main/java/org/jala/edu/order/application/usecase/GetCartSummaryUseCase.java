package org.jala.edu.order.application.usecase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CartItem;

/**
 * Caso de uso: armar el resumen del carrito de un cliente para mostrarlo en pantalla.
 * El carrito solo guarda ids y cantidades; los nombres y precios se piden al catalogo.
 */
public class GetCartSummaryUseCase {

    /** Para buscar el carrito del cliente. */
    private final CartRepositoryPort carts;

    /** Para obtener nombre y precio de cada producto. */
    private final CatalogService catalog;

    /**
     * @param carts   repositorio de carritos
     * @param catalog servicio del catalogo
     */
    public GetCartSummaryUseCase(CartRepositoryPort carts, CatalogService catalog) {
        this.carts = carts;
        this.catalog = catalog;
    }

    /**
     * Calcula el resumen del carrito. Si el cliente no tiene productos, devuelve un resumen vacio con totales en cero.
     * <ol>
     *   <li>Para cada producto del carrito calcula: subtotal de la linea = precio unitario x cantidad.</li>
     *   <li>Guarda cada linea en la lista de su comercio.</li>
     *   <li>Suma las lineas para el subtotal, agrega el envio (solo si hay productos) y calcula el total.</li>
     * </ol>
     *
     * @param customerId id del cliente
     * @return resumen con las lineas por comercio, el subtotal, el envio y el total
     * @throws NotFoundException si algun producto del carrito ya no existe en el catalogo
     */
    public CartSummary execute(UUID customerId) {
        Cart cart = carts.findOrCreate(customerId);

        // Mapa: id del comercio -> lineas de ese comercio.
        // LinkedHashMap es un mapa que conserva el orden en que se agregaron los comercios.
        Map<UUID, List<CartSummary.Line>> linesByCommerce = new LinkedHashMap<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            // orElse(null): saca el producto del Optional, o null si no existe
            ProductInfo product = catalog.findProduct(item.getProductId()).orElse(null);
            if (product == null) {
                throw new NotFoundException("Product not found");
            }
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            BigDecimal lineSubtotal = product.price().multiply(quantity);
            CartSummary.Line line = new CartSummary.Line(item.getProductId(), product.name(), product.price(),
                    item.getQuantity(), lineSubtotal);

            // Busca la lista de este comercio; si todavia no existe, la crea
            List<CartSummary.Line> commerceLines = linesByCommerce.get(item.getCommerceId());
            if (commerceLines == null) {
                commerceLines = new ArrayList<>();
                linesByCommerce.put(item.getCommerceId(), commerceLines);
            }
            commerceLines.add(line);
            subtotal = subtotal.add(lineSubtotal);
        }

        // Convierte cada comercio del mapa en un grupo
        List<CartSummary.Group> groups = new ArrayList<>();
        for (UUID commerceId : linesByCommerce.keySet()) {
            groups.add(new CartSummary.Group(commerceId, linesByCommerce.get(commerceId)));
        }

        // El envio solo se cobra si hay productos
        BigDecimal shipping = BigDecimal.ZERO;
        if (!groups.isEmpty()) {
            shipping = Cart.SHIPPING_COST;
        }
        BigDecimal total = subtotal.add(shipping);

        return new CartSummary(groups, subtotal, shipping, total);
    }
}