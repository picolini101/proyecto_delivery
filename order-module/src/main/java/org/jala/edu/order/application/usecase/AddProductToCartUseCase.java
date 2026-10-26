package org.jala.edu.order.application.usecase;

import java.util.UUID;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.ProductInfo;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.ConflictException;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;

public class AddProductToCartUseCase {

    private final CartRepositoryPort carts;
    private final CatalogService catalog;
    private final UserService users;

    public AddProductToCartUseCase(CartRepositoryPort carts, CatalogService catalog, UserService users) {
        this.carts = carts;
        this.catalog = catalog;
        this.users = users;
    }

    public Cart execute(UUID customerId, UUID productId, int quantity) {
        if (!users.isActiveWithRole(customerId, UserRole.CUSTOMER)) {
            throw new DomainException("User not authorized for this operation");
        }
        ProductInfo product = catalog.findProduct(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Cart cart = carts.findByCustomerId(customerId).orElseGet(() -> new Cart(customerId));
        if (!catalog.isAvailable(productId, cart.quantityOf(productId) + quantity)) {
            throw new ConflictException("No hay stock suficiente para esa cantidad");
        }
        cart.addProduct(productId, product.commerceId(), quantity);
        return carts.save(cart);
    }
}
