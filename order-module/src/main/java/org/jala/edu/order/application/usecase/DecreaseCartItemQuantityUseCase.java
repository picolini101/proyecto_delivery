package org.jala.edu.order.application.usecase;

import java.util.UUID;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;

/**
 * Caso de uso del boton "-" del carrito: quita unidades a un producto que ya esta en el carrito.
 *
 * Quitar un producto por completo (el icono de basura) no esta aqui, la cantidad nunca baja de 1.
 * Como solo baja la cantidad, no hace falta preguntar nada al catalogo.
 */

public class DecreaseCartItemQuantityUseCase {

    /** Para buscar y guardar el carrito del cliente. */
    private final CartRepositoryPort carts;

    /** Para validar que el cliente existe, esta activo y tiene rol CUSTOMER. */
    private final UserService users;

    /**
     * @param carts repositorio de carritos
     * @param users servicio de usuarios
     */
    public DecreaseCartItemQuantityUseCase(CartRepositoryPort carts, UserService users) {
        this.carts = carts;
        this.users = users;
    }

    /**
     * Quita unidades a un producto del carrito. Pasos:
     *
     * Valida que el cliente este activo y sea CUSTOMER.
     * Busca el carrito del cliente.
     * Le pide al carrito que quite las unidades.
     * Guarda el carrito.
     *
     *
     * @param customerId id del cliente
     * @param productId  id del producto que ya esta en el carrito
     * @param quantity   unidades a QUITAR (no el total nuevo), debe ser mayor que cero
     * @return el carrito actualizado
     * @throws DomainException   si el cliente no esta autorizado, la cantidad no es valida o quedaria en menos de 1
     */

    public Cart execute(UUID customerId, UUID productId, int quantity){
        if(!users.isActiveWithRole(customerId, UserRole.CUSTOMER)){
            throw new DomainException("User not autorized for this oepration");
        }

        Cart cart = carts.findOrCreate(customerId);
        cart.decreaseProduct(productId, quantity);
        return carts.save(cart);
    }
}
