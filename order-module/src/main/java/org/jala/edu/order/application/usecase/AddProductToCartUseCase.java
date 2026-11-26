package org.jala.edu.order.application.usecase;

import java.util.UUID;


import org.jala.edu.order.application.port.out.CatalogPort;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;

/**
 * Caso de uso HU1: agregar un producto al carrito de un cliente.
 * Un caso de uso es una accion completa del sistema; aqui junta tres cosas:
 * los usuarios (para validar al cliente), el catalogo (producto y stock) y el repositorio (guardar el carrito).
 */
public class AddProductToCartUseCase {

    /** Para buscar y guardar el carrito del cliente. */
    private final CartRepositoryPort carts;

    /** Para consultar el producto y saber si hay stock. Lo publica el modulo de catalogo. */
    private final CatalogPort catalog;

    /** Para validar que el cliente existe, esta activo y tiene rol CUSTOMER. */
    private final UserService users;

    /**
     * Recibe las dependencias que necesita. Spring las entrega desde {@code OrderModuleConfiguration}.
     *
     * @param carts   repositorio de carritos
     * @param catalog servicio del catalogo
     * @param users   servicio de usuarios
     */
    public AddProductToCartUseCase(CartRepositoryPort carts, CatalogPort catalog, UserService users) {
        this.carts = carts;
        this.catalog = catalog;
        this.users = users;
    }

    /**
     * Agrega una cantidad de un producto al carrito del cliente. Pasos:
     * <ol>
     *   <li>Valida que el cliente este activo y sea CUSTOMER.</li>
     *   <li>Busca el producto en el catalogo (si no existe, falla).</li>
     *   <li>Busca el carrito del cliente (si no tiene, recibe uno nuevo vacio).</li>
     *   <li>Revisa que el stock alcance para lo que ya habia en el carrito MAS la cantidad nueva.</li>
     *   <li>Agrega el producto al carrito y lo guarda.</li>
     * </ol>
     *
     * @param customerId id del cliente
     * @param productId  id del producto a agregar
     * @param quantity   cantidad a agregar (debe ser mayor que cero)
     * @return el carrito actualizado
     * @throws DomainException   si el cliente no esta autorizado o la cantidad no es valida
     * @throws NotFoundException si el producto no existe
     * @throws ConflictException si no hay stock suficiente
     */
    public Cart execute(UUID customerId, UUID productId, int quantity) {
        // 1. Validar al cliente
        if (!users.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new DomainException("User not authorized for this operation");
        }

        // 2. Buscar el producto.
        // findProduct devuelve un "Optional": una caja que puede tener el producto o estar vacia.
        // orElse(null) abre la caja: da el producto, o null si la caja estaba vacia.
        ProductInfo product = catalog.findProduct(productId).orElse(null);
        if (product == null) {
            throw new NotFoundException("Product not found");
        }

        // 3. Carrito del cliente (existente o nuevo)
        Cart cart = carts.findOrCreate(customerId);

        // 4. Validar stock contando lo que ya esta en el carrito
        int totalQuantity = cart.quantityOf(productId) + quantity;
        if (!catalog.isAvailable(productId, totalQuantity)) {
            throw new ConflictException("No hay stock suficiente para esa cantidad");
        }

        // 5. Agregar y guardar
        cart.addProduct(productId, product.commerceId(), quantity, product.price());
        return carts.save(cart);
    }
}