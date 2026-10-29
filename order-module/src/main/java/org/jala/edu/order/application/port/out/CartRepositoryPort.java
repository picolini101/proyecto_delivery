package org.jala.edu.order.application.port.out;
import java.util.UUID;
import org.jala.edu.order.domain.model.Cart;

/**
 * Puerto de salida: define COMO el modulo guarda y busca carritos, sin decir DONDE.
 * Los casos de uso solo conocen esta interfaz. La clase que la implementa (por ejemplo
 * {@code InMemoryCartRepositoryAdapter}) decide si se guarda en memoria o en una base de datos.
 * Asi se puede cambiar el almacenamiento sin tocar los casos de uso.
 */
public interface CartRepositoryPort {

    /**
     * Busca el carrito de un cliente. Si el cliente todavia no tiene carrito, devuelve uno nuevo y vacio
     * (este carrito nuevo no queda guardado hasta que se llame a {@link #save(Cart)}).
     *
     * @param customerId id del cliente
     * @return el carrito del cliente, nunca es null
     */
    Cart findOrCreate(UUID customerId);

    /**
     * Guarda el carrito: lo crea si es nuevo o lo reemplaza si ya existia.
     *
     * @param cart carrito a guardar
     * @return el mismo carrito, ya guardado
     */
    Cart save(Cart cart);
}