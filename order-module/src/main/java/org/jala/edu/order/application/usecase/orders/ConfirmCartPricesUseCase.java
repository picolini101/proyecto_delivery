package org.jala.edu.order.application.usecase.orders;

import java.util.List;
import java.util.UUID;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.CatalogUnavailableException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;

/**
 * Caso de uso: el cliente acepta los precios nuevos de los productos de su carrito.
 * Solo se aceptan los precios que el cliente VIO (los que muestra el aviso): si el catalogo ya los cambio
 * otra vez, ese producto queda sin aceptar y la finalizacion de compra volvera a pedir confirmacion.
 */
public class ConfirmCartPricesUseCase {

    /** Para validar que el cliente existe, esta activo y tiene rol CUSTOMER. */
    private final UserService users;

    /** Para buscar y guardar el carrito del cliente. */
    private final CartRepositoryPort carts;

    /** Para consultar los precios vigentes del catalogo. */
    private final CartVerifier verifier;

    /**
     * @param users    servicio de usuarios
     * @param carts    repositorio de carritos
     * @param verifier verificador de carritos (consulta el catalogo)
     */
    public ConfirmCartPricesUseCase(UserService users, CartRepositoryPort carts, CartVerifier verifier) {
        this.users = users;
        this.carts = carts;
        this.verifier = verifier;
    }

    /**
     * Guarda en el carrito los precios nuevos que el cliente acepto.
     *
     * @param customerId id del cliente
     * @param accepted   cambios de precio que el cliente vio y acepto
     * @return el carrito actualizado
     * @throws UnauthorizedCustomerException si el usuario no es un cliente activo
     * @throws CatalogUnavailableException   si el catalogo no responde (el carrito no cambia)
     */
    public Cart execute(UUID customerId, List<CartVerification.PriceChange> accepted) {
        if (!users.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }
        Cart cart = carts.findOrCreate(customerId);
        for (CartVerification.PriceChange current : verifier.verify(cart).priceChanges()) {
            if (wasAccepted(accepted, current)) {
                cart.confirmPrice(current.productId(), current.newPrice());
            }
        }
        return carts.save(cart);
    }

    /**
     * @param accepted cambios que el cliente acepto
     * @param current  cambio vigente del catalogo
     * @return true si el cliente acepto justo este producto con este precio nuevo
     */
    private boolean wasAccepted(List<CartVerification.PriceChange> accepted, CartVerification.PriceChange current) {
        for (CartVerification.PriceChange change : accepted) {
            if (change.productId().equals(current.productId()) && change.newPrice().compareTo(current.newPrice()) == 0) {
                return true;
            }
        }
        return false;
    }
}