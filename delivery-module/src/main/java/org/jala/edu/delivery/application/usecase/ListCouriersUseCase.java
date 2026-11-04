package org.jala.edu.delivery.application.usecase;

import java.util.List;
import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.delivery.application.port.in.ListCouriersQuery;
import org.jala.edu.delivery.application.port.out.CourierRepositoryPort;
import org.jala.edu.delivery.domain.model.Courier;

/**
 * Caso de uso para consultar los repartidores registrados.
 */
public class ListCouriersUseCase implements UseCase<ListCouriersQuery, List<Courier>> {

    private final CourierRepositoryPort couriers;

    public ListCouriersUseCase(CourierRepositoryPort couriers) {
        this.couriers = couriers;
    }

    @Override
    public List<Courier> execute(ListCouriersQuery query) {
        return couriers.findAll();
    }
}