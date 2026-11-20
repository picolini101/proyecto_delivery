package org.jala.edu.order.infrastructure.config;

import org.jala.edu.core.contract.CatalogService;
import org.jala.edu.core.contract.PaymentService;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.port.out.OrderRepositoryPort;
import org.jala.edu.order.application.port.out.OrderStatusHistoryPort;
import org.jala.edu.order.application.port.out.PaymentGatewayPort;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.application.usecase.AddProductToCartUseCase;
import org.jala.edu.order.application.usecase.DecreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.GetCartSummaryUseCase;
import org.jala.edu.order.application.usecase.IncreaseCartItemQuantityUseCase;
import org.jala.edu.order.application.usecase.ListCatalogProductsUseCase;
import org.jala.edu.order.application.usecase.orders.InputAddressUseCase;
import org.jala.edu.order.application.usecase.orders.*;
import org.jala.edu.order.application.usecase.RemoveProductFromCartUseCase;
import org.jala.edu.order.infrastructure.adapter.out.payment.PaymentServiceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los casos de uso del modulo de pedidos en el contenedor de Spring.
 */
@Configuration
public class OrderModuleConfiguration {

    @Bean
    public AddProductToCartUseCase addProductToCartUseCase(CartRepositoryPort carts, CatalogService catalog,
                                                           UserService users) {
        return new AddProductToCartUseCase(carts, catalog, users);
    }

    @Bean
    public GetCartSummaryUseCase getCartSummaryUseCase(CartRepositoryPort carts, CatalogService catalog) {
        return new GetCartSummaryUseCase(carts, catalog);
    }

    @Bean
    public ListCatalogProductsUseCase listCatalogProductsUseCase(CatalogService catalog) {
        return new ListCatalogProductsUseCase(catalog);
    }

    @Bean
    public IncreaseCartItemQuantityUseCase increaseCartItemQuantityUseCase(CartRepositoryPort carts,
                                                                           CatalogService catalog,
                                                                           UserService users) {
        return new IncreaseCartItemQuantityUseCase(carts, catalog, users);
    }

    @Bean
    public DecreaseCartItemQuantityUseCase decreaseCartItemQuantityUseCase(CartRepositoryPort carts,
                                                                           UserService users) {
        return new DecreaseCartItemQuantityUseCase(carts, users);
    }

    @Bean
    public RemoveProductFromCartUseCase removeProductFromCartUseCase(CartRepositoryPort carts) {
        return new RemoveProductFromCartUseCase(carts);
    }

    @Bean
    public CartVerifier cartVerifier(CatalogService catalog) {
        return new CartVerifier(catalog);
    }
    @Bean
    public StartCheckoutUseCase startCheckoutUseCase(
            UserService users,
            CartRepositoryPort carts,
            CartVerifier cartVerifier,
            OrderRepositoryPort orderRepository
    ) {
        return new StartCheckoutUseCase(users, carts, cartVerifier, orderRepository);
    }


    /**
     * Adaptador del puerto de pagos (INT-002): traduce el contrato {@link PaymentService} del core.
     */
    @Bean
    public PaymentGatewayPort paymentGatewayPort(PaymentService paymentService) {
        return new PaymentServiceAdapter(paymentService);
    }

    /**
     * Caso de uso HU-10: autorizar el pago de un pedido por el monto total.
     */
    @Bean
    public AuthorizePaymentUseCase authorizePaymentUseCase(UserService users, OrderRepositoryPort orderRepository,
                                                           PaymentGatewayPort paymentGateway) {
        return new AuthorizePaymentUseCase(users, orderRepository, paymentGateway);
    }

    @Bean
    public InputAddressUseCase inputAddressUseCase(OrderRepositoryPort orders) {
        return new InputAddressUseCase(orders);

    }

    @Bean
    public PreviewCheckoutSummaryUseCase previewCheckoutSummaryUseCase(
            UserService users, CartRepositoryPort carts, CartVerifier cartVerifier) {
        return new PreviewCheckoutSummaryUseCase(users, carts, cartVerifier);
    }

    @Bean
    public ChangeOrderStatusUseCase changeOrderStatusUseCase(OrderRepositoryPort orderRepository,
                                                             OrderStatusHistoryPort statusHistory,
                                                             org.jala.edu.core.application.port.out.TransactionRunner transactions) {
        return new ChangeOrderStatusUseCase(orderRepository, statusHistory, transactions);
    }

    @Bean
    public ConfirmOrderUseCase confirmOrderUseCase(OrderRepositoryPort orderRepository,
                                                   OrderStatusHistoryPort statusHistory,
                                                   org.jala.edu.core.application.port.out.TransactionRunner transactions) {
        return new ConfirmOrderUseCase(orderRepository, statusHistory, transactions);
    }
}
