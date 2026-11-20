package org.jala.edu.order.application.usecase.orders;

import org.jala.edu.core.application.usecase.UseCase;
import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.order.application.dto.CheckoutSummary;
import org.jala.edu.order.application.port.in.StartCheckoutCommand; // Reutilizamos el command de customer
import org.jala.edu.order.application.port.out.CartRepositoryPort;
import org.jala.edu.order.application.service.CartVerification;
import org.jala.edu.order.application.service.CartVerifier;
import org.jala.edu.order.domain.exception.UnauthorizedCustomerException;
import org.jala.edu.order.domain.model.Cart;

/**
 * Caso de uso: Generar el resumen previo al pago.
 * Valida al cliente, recupera su carrito y verifica contra el catálogo
 * que los productos existan y tengan stock, calculando además los totales.
 */

public class PreviewCheckoutSummaryUseCase implements UseCase<StartCheckoutCommand, CheckoutSummary> {

    private final UserService userService;
    private final CartRepositoryPort cartRepository;
    private final CartVerifier cartVerifier;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param userService    Servicio para validar el rol del usuario.
     * @param cartRepository Repositorio para obtener el carrito actual.
     * @param cartVerifier   Servicio de dominio para verificar precios y stock.
     */
    public PreviewCheckoutSummaryUseCase(UserService userService, CartRepositoryPort cartRepository, CartVerifier cartVerifier) {
        this.userService = userService;
        this.cartRepository = cartRepository;
        this.cartVerifier = cartVerifier;
    }

    @Override
    public CheckoutSummary execute(StartCheckoutCommand command) {
        if (!userService.isActiveWithRole(command.customerId(), UserRole.CUSTOMER)) {
            throw new UnauthorizedCustomerException();
        }

        Cart cart = cartRepository.findOrCreate(command.customerId());
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Tu carrito está vacío"); // O usar tu EmptyCartException
        }
        CartVerification verification = cartVerifier.verify(cart);

        return CheckoutSummary.from(verification);
    }
}