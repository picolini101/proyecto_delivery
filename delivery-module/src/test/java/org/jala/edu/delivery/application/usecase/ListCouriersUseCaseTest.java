package org.jala.edu.delivery.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jala.edu.delivery.application.port.in.ListCouriersQuery;
import org.jala.edu.delivery.domain.model.Courier;
import org.jala.edu.delivery.domain.model.TransportMode;
import org.jala.edu.delivery.infrastructure.adapter.out.persistence.memory.InMemoryCourierRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ListCouriersUseCaseTest {

    private final InMemoryCourierRepositoryAdapter repository =
            new InMemoryCourierRepositoryAdapter();

    private final ListCouriersUseCase listCouriers =
            new ListCouriersUseCase(repository);

    @Test
    void returnsAllRegisteredCouriers() {
        // Arrange
        Courier first = Courier.register(
                "Maria Perez", "3001234567", TransportMode.BICYCLE
        );

        Courier second = Courier.register(
                "Juan Lopez", "3101234567", TransportMode.MOTORCYCLE
        );

        repository.save(first);
        repository.save(second);

        // Act
        List<Courier> result = listCouriers.execute(new ListCouriersQuery());

        // Assert
        assertThat(result).containsExactly(first, second);
    }

    @Test
    void returnsAnEmptyListWhenThereAreNoCouriers() {
        // Act
        List<Courier> result = listCouriers.execute(new ListCouriersQuery());

        // Assert
        assertThat(result).isEmpty();
    }
}