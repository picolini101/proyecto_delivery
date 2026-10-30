package org.jala.edu.order.application.usecase;

import java.util.UUID;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;
import org.jala.edu.order.domain.model.CartItem;

/**
 * Caso de uso HU4: agregar MAS unidades de un producto que ya esta en el carrito.
 *
 * Tiene un parecido a {@link AddProductToCartUseCase} (HU1), pero con una diferencia: aqui el producto tiene que
 * estar ya en el carrito. Si no esta, falla en vez de crear una linea nueva. Asi nunca se crea una
 * segunda fila del mismo producto.
 *
 * No se modifico ninguna clase de HU1: este caso de uso reutiliza {@link Cart#addProduct(UUID, UUID, int)},
 * que ya suma la cantidad cuando el producto existe.
 */

public class IncreaseCartItemQuantityUseCase {

    /** Para buscar y guardar el carrito del cliente. */
    private final CartRepositoryPort carts;

    /** Para preguntar si hay stock, lo publica el modulo de catalogo. */
    private final CatalogService catalog;

    /** Para validar que el cliente existe, esta activo y tiene rol CUSTOMER. */
    private final UserService users;

    /**
     * Recibe las dependencias que necesita.
     *
     * @param carts   repositorio de carritos
     * @param catalog servicio del catalogo
     * @param users   servicio de usuarios
     */
    public IncreaseCartItemQuantityUseCase(CartRepositoryPort carts, CatalogService catalog, UserService users) {
        this.carts = carts;
        this.catalog = catalog;
        this.users = users;
    }

    /**
     * Suma unidades a un producto que ya esta en el carrito. Pasos:
     *
     * Valida que el cliente este activo y sea CUSTOMER.
     * Valida que la cantidad a agregar sea mayor que cero.
     * Busca el carrito del cliente y la linea del producto.
     * Pregunta al catalogo si alcanza el stock para lo que ya habia MAS lo nuevo.
     * Si alcanza, suma la cantidad y guarda el carrito.
     *
     *
     * @param customerId id del cliente
     * @param productId  id del producto que ya esta en el carrito
     * @param quantity   unidades a AGREGAR (no el total nuevo), debe ser mayor que cero
     * @return el carrito actualizado
     * @throws DomainException   si el cliente no esta autorizado o la cantidad no es valida
     * @throws NotFoundException si el producto no esta en el carrito
     * @throws ConflictException si el stock no alcanza para la cantidad acumulada
     */
    public Cart execute(UUID customerId, UUID productId, int quantity) {
        if (!users.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new DomainException("User not authorized for this operation");
        }

        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }

        Cart cart = carts.findOrCreate(customerId);
        CartItem item = cart.findItem(productId);
        if (item == null) {
            throw new NotFoundException("El producto no está en el carrito");
        }

        long totalQuantity = (long) item.getQuantity() + quantity;
        if (totalQuantity > Integer.MAX_VALUE || !catalog.isAvailable(productId, (int) totalQuantity)) {
            throw new ConflictException("No se tienen suficientes unidades en stock");
        }

        //Sumar y guardar addProduct suma a la linea existente.
        cart.addProduct(productId, item.getCommerceId(), quantity);
        return carts.save(cart);
    }

}
