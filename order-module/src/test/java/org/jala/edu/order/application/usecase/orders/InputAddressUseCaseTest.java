package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.jala.edu.order.application.dto.OrderSummary;
import org.jala.edu.order.application.port.in.InputAddressCommand;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.OrderNotFoundException;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InputAddressUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private InputAddressUseCase useCase;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        useCase = new InputAddressUseCase(orderRepository);

        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()), "Producto de prueba", Money.of(new BigDecimal("25.00")),
                Quantity.of(1));
        order = Order.create(new CustomerId(UUID.randomUUID()), java.util.List.of(line), Money.of(25));
        orderRepository.save(order);
    }

    @Test
    @DisplayName("Guarda la dirección validada asociada al pedido")
    void shouldSaveAddressForOrder() {
        InputAddressCommand command = new InputAddressCommand(
                order.getId().value(), order.getCustomerId().value(), "  La Paz  ", "  Avenida Arce  ", "  123  ");

        OrderSummary summary = useCase.execute(command);

        Order savedOrder = orderRepository.findById(OrderId.of(order.getId().value())).orElseThrow();
        assertThat(summary.orderId()).isEqualTo(order.getId().value());
        assertThat(savedOrder.getDeliveryAddress())
                .isEqualTo(new DeliveryAddress("La Paz", "Avenida Arce", "123"));
    }

    @Test
    @DisplayName("Rechaza la dirección cuando un campo obligatorio está vacío")
    void shouldRejectAddressWithBlankRequiredField() {
        InputAddressCommand command = new InputAddressCommand(
                order.getId().value(), order.getCustomerId().value(), "La Paz", "   ", "123");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("avenida");
        assertThat(order.getDeliveryAddress()).isNull();
    }

    @Test
    @DisplayName("Falla cuando el pedido indicado no existe")
    void shouldFailWhenOrderDoesNotExist() {
        InputAddressCommand command = new InputAddressCommand(
                UUID.randomUUID(), order.getCustomerId().value(), "La Paz", "Avenida Arce", "123");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("Rechaza cambiar la dirección de un pedido ajeno")
    void shouldRejectAddressForOrderOwnedByAnotherCustomer() {
        InputAddressCommand command = new InputAddressCommand(
                order.getId().value(), UUID.randomUUID(), "La Paz", "Avenida Arce", "123");

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedCustomerException.class);
        assertThat(order.getDeliveryAddress()).isNull();
    }
}
