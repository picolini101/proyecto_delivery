package org.jala.edu.order.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jala.edu.core.domain.exception.DomainException;

/**
 * Carrito de compras de un cliente. Es una clase del dominio: solo tiene datos y reglas simples,
 * no sabe nada de pantallas ni de bases de datos.
 * Cada cliente tiene un solo carrito, y puede tener productos de cualquier comercio.
 */
public class Cart {

    /** Costo fijo de envio que se suma al total del carrito. */
    public static final BigDecimal SHIPPING_COST = new BigDecimal("10.00");

    /** Cliente al que pertenece el carrito. */
    private final UUID customerId;

    /** Lineas del carrito: una por cada producto distinto. */
    private final List<CartItem> items = new ArrayList<>();

    /**
     * Crea un carrito vacio.
     *
     * @param customerId id del cliente
     */
    public Cart(UUID customerId) {
        this.customerId = customerId;
    }

    /** @return id del cliente al que pertenece el carrito */
    public UUID getCustomerId() { return customerId; }

    /**
     * @return una COPIA de las lineas del carrito; asi nadie desde afuera puede agregar o quitar lineas
     *         sin pasar por {@link #addProduct(UUID, UUID, int)}
     */
    public List<CartItem> getItems() { return new ArrayList<>(items); }

    /**
     * Busca la linea de un producto en el carrito.
     *
     * @param productId id del producto
     * @return la linea, o {@code null} si el producto no esta en el carrito
     */
    public CartItem findItem(UUID productId) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Cantidad de un producto que hay en el carrito.
     *
     * @param productId id del producto
     * @return la cantidad, o 0 si el producto no esta
     */
    public int quantityOf(UUID productId) {
        CartItem item = findItem(productId);
        if (item == null) {
            return 0;
        }
        return item.getQuantity();
    }

    /**
     * Agrega un producto al carrito. Si el producto ya estaba, suma la cantidad a su linea;
     * si no estaba, crea una linea nueva.
     *
     * @param productId  id del producto
     * @param commerceId id del comercio que vende el producto
     * @param quantity   cantidad a agregar, debe ser mayor que cero
     * @throws DomainException si la cantidad es cero o negativa
     */
    public void addProduct(UUID productId, UUID commerceId, int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
        CartItem item = findItem(productId);
        if (item == null) {
            items.add(new CartItem(productId, commerceId, quantity));
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }
    }
}