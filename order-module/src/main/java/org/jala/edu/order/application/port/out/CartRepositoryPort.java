package org.jala.edu.order.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.jala.edu.order.domain.model.Cart;

public interface CartRepositoryPort {

    Optional<Cart> findByCustomerId(UUID customerId);

    Cart save(Cart cart);
}
