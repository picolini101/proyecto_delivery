package org.jala.edu.order.application.usecase.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.exception.NotFoundException;
import org.jala.edu.order.application.dto.OrderConfirmationReceipt;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.CustomerId;
import org.jala.edu.order.domain.model.DeliveryAddress;
import org.jala.edu.order.domain.model.Money;
import org.jala.edu.order.domain.model.Order;
import org.jala.edu.order.domain.model.OrderId;
import org.jala.edu.order.domain.model.OrderLine;
import org.jala.edu.order.domain.model.OrderStatus;
import org.jala.edu.order.domain.model.ProductId;
import org.jala.edu.order.domain.model.Quantity;
import org.jala.edu.order.infrastructure.adapter.out.persistence.memory.InMemoryOrderRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetOrderConfirmationUseCaseTest {

    private OrderRepositoryPort orderRepository;
    private GetOrderConfirmationUseCase useCase;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepositoryAdapter();
        useCase = new GetOrderConfirmationUseCase(orderRepository);
    }

    private Order createConfirmedSampleOrder() {
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Hamburguesa Clásica",
                Money.of(new BigDecimal("45.00")),
                Quantity.of(2)
        );
        Order order = Order.create(CustomerId.of(customerId), List.of(line), line.getSubtotal());
        order.inputDeliveryAddress(new DeliveryAddress("Av. Principal 123", "Piso 4", "Zona Central"));
        order.transitionTo(OrderStatus.CONFIRMED, null);
        return orderRepository.save(order);
    }

    @Test
    @DisplayName("CA-1 y CA-2: Retornar comprobante detallado con código de entrega de 4 dígitos para pedido CONFIRMED")
    void shouldReturnDetailedReceiptWhenOrderIsConfirmed() {
        // Arrange
        Order order = createConfirmedSampleOrder();

        // Act
        OrderConfirmationReceipt receipt = useCase.execute(order.getId().value(), customerId);

        // Assert
        assertThat(receipt).isNotNull();
        assertThat(receipt.orderId()).isEqualTo(order.getId().value());
        assertThat(receipt.deliveryCode()).isEqualTo(order.getDeliveryCode());
        assertThat(receipt.deliveryCode()).hasSize(4);
        assertThat(receipt.status()).isEqualTo("CONFIRMED");
        assertThat(receipt.lines()).hasSize(1);
        assertThat(receipt.lines().get(0).productName()).isEqualTo("Hamburguesa Clásica");
        assertThat(receipt.total()).isNotNull();
        assertThat(receipt.deliveryAddress()).contains("Av. Principal 123");
    }

    @Test
    @DisplayName("CA-3: Rechazar consulta si el pedido no pertenece al cliente solicitante")
    void shouldRejectWhenCustomerIsNotOwner() {
        // Arrange
        Order order = createConfirmedSampleOrder();
        UUID otherCustomer = UUID.randomUUID();

        // Act & Assert
        try {
            useCase.execute(order.getId().value(), otherCustomer);
            fail("Debió lanzar UnauthorizedCustomerException por pertenecer a otro cliente");
        } catch (UnauthorizedCustomerException ex) {
            assertThat(ex.getMessage()).contains("no pertenece al cliente");
        }
    }

    @Test
    @DisplayName("CA-4: Rechazar emisión de comprobante si el pedido aún no está confirmado (ej. CREATED)")
    void shouldRejectWhenOrderIsNotYetConfirmed() {
        // Arrange: Pedido en CREATED sin pagar
        OrderLine line = OrderLine.of(
                ProductId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                "Refresco",
                Money.of(new BigDecimal("10.00")),
                Quantity.of(1)
        );
        Order order = Order.create(CustomerId.of(customerId), List.of(line), line.getSubtotal());
        orderRepository.save(order);

        // Act & Assert
        try {
            useCase.execute(order.getId().value(), customerId);
            fail("Debió lanzar DomainException al no estar confirmado");
        } catch (DomainException ex) {
            assertThat(ex.getMessage()).contains("no ha sido confirmado");
        }
    }

    @Test
    @DisplayName("CA-5: Rechazar si el ID del pedido no existe")
    void shouldRejectWhenOrderDoesNotExist() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        try {
            useCase.execute(nonExistentId, customerId);
            fail("Debió lanzar NotFoundException");
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).contains("no encontrado");
        }
    }
}