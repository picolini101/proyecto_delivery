package org.jala.edu.order.infrastructure.adapter.out.persistence.memory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.domain.model.Cart;
import org.springframework.stereotype.Component;

/**
 * Implementacion de {@link CartRepositoryPort} que guarda los carritos en memoria, uno por cliente.
 * Se activa solo cuando {@code delivery.persistence.type=memory}. Los datos se pierden al cerrar la aplicacion.
 * {@code @Component} hace que Spring cree este objeto automaticamente.
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryCartRepositoryAdapter implements CartRepositoryPort {

    /**
     * Mapa: id del cliente -> su carrito.
     * ConcurrentHashMap funciona igual que un HashMap, pero es seguro cuando varios hilos lo usan a la vez
     * (la pantalla pide varias cosas en segundo plano al mismo tiempo).
     */
    private final Map<UUID, Cart> carts = new ConcurrentHashMap<>();

    /**
     * @param customerId id del cliente
     * @return el carrito guardado del cliente, o un carrito nuevo y vacio si no tiene
     */
    @Override
    public Cart findOrCreate(UUID customerId) {
        Cart cart = carts.get(customerId);
        if (cart == null) {
            cart = new Cart(customerId);
        }
        return cart;
    }

    /**
     * Guarda el carrito usando el id del cliente como llave; si ya habia uno, lo reemplaza.
     *
     * @param cart carrito a guardar
     * @return el mismo carrito
     */
    @Override
    public Cart save(Cart cart) {
        carts.put(cart.getCustomerId(), cart);
        return cart;
    }
}