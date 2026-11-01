package org.jala.edu.delivery.application.port.out;

import java.util.UUID;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.delivery.domain.model.Courier;

/**
 * Define las operaciones de almacenamiento de repartidores.
 */
public interface CourierRepositoryPort extends CrudRepositoryPort<Courier, UUID> {
}