package org.jala.edu.order.application.usecase;

import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;

import java.util.Objects;
import java.util.UUID;

public class RemoveProductFromCartUseCase {
    private final CartRepositoryPort cartRepository;

    public RemoveProductFromCartUseCase(CartRepositoryPort cartRepository){
        this.cartRepository = Objects.requireNonNull(cartRepository, "cartRepository can't be null");
    }
    public Cart execute(UUID customerId, UUID productId){
        Objects.requireNonNull(customerId, "CustomerId can't be null");
        Objects.requireNonNull(productId, "productId can't be null");

        Cart cart = cartRepository.findOrCreate(customerId);
        cart.removeItem(productId);
        return cartRepository.save(cart);
    }
}
