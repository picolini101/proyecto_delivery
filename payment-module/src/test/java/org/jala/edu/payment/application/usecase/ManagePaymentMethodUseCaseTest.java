
package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.in.RegisterPaymentMethodCommand;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentMethod;
import org.jala.edu.payment.domain.model.Alias;
import org.jala.edu.payment.domain.model.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagePaymentMethodUseCaseTest {

    @Mock
    private PaymentMethodRepositoryPort repository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ManagePaymentMethodUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("CA1 & CA2: Debe registrar un método de pago correctamente cuando el usuario está autorizado")
    void shouldRegisterPaymentMethodSuccessfully() {
        // Preparación.
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Mi Tarjeta Principal", "TARJETA", "4532123456789012"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);
        when(repository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejuecución.
        PaymentMethod result = useCase.registerPaymentMethod(command);

        // Verificación.
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Mi Tarjeta Principal", result.getAlias().getValue());
        assertEquals(PaymentType.TARJETA, result.getType());
        assertTrue(result.isLastRegistered());
        assertTrue(result.isActive());

        // Verificaciones de comportamiento
        verify(userService).isActiveWithRole(userId, UserRole.CUSTOMER);
        verify(repository).clearLastRegisteredFlag(userId);
        verify(repository).save(any(PaymentMethod.class));
    }

    @Test
    @DisplayName("CA4: Debe lanzar DomainException cuando el usuario no está activo o no tiene rol CUSTOMER")
    void shouldThrowExceptionWhenUserIsNotAuthorized() {
        // Preparación.
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Alias Valido", "TARJETA", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(false);

        // Ejecución y Verificación.
        DomainException exception = assertThrows(DomainException.class, () ->
                useCase.registerPaymentMethod(command)
        );

        assertEquals("User not authorized for this operation", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA3: Debe lanzar DomainException cuando el tipo de pago es inválido")
    void shouldThrowExceptionWhenPaymentTypeIsInvalid() {
        // Preparación.
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Alias Valido", "PAYPAL_INVALIDO", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);

        // Ejecución y Verificación.
        DomainException exception = assertThrows(DomainException.class, () ->
                useCase.registerPaymentMethod(command)
        );

        assertTrue(exception.getMessage().contains("Tipo de pago inválido"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA3: Debe lanzar DomainException cuando el alias es nulo o vacío")
    void shouldThrowExceptionWhenAliasIsEmpty() {
        // Preparación.
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "  ", "TRANSFERENCIA", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);

        // Ejecución y Verificación.
        assertThrows(DomainException.class, () -> useCase.registerPaymentMethod(command));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA4: Debe listar métodos de pago cuando el usuario es CUSTOMER autorizado")
    void shouldListPaymentMethodsSuccessfully() {
        // Preparación.
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);
        when(repository.findByUserId(userId)).thenReturn(List.of());

        // Ejecución.
        List<PaymentMethod> result = useCase.listPaymentMethods(userId);

        // Verificación.
        assertNotNull(result);
        verify(repository).findByUserId(userId);
    }

    @Test
    @DisplayName("CA1: Debe lanzar DomainException cuando el alias ya existe para el usuario")
    void shouldThrowExceptionWhenAliasIsDuplicated() {
        // Preparación.
        PaymentMethod existing = PaymentMethod.createNew(
                userId, new Alias("Mi Tarjeta"), PaymentType.TARJETA, "1111");
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "mi tarjeta", "TRANSFERENCIA", "2222");
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);
        when(repository.findByUserId(userId)).thenReturn(List.of(existing));

        // Ejecución y Validación.
        DomainException exception = assertThrows(DomainException.class,
                () -> useCase.registerPaymentMethod(command));

        assertEquals("Ya tienes un método de pago con ese alias", exception.getMessage());
        verify(repository, never()).clearLastRegisteredFlag(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA4: Debe lanzar DomainException al listar si el usuario no está autorizado")
    void shouldThrowExceptionWhenListingWithUnauthorizedUser() {
        // Preparación.
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(false);

        // Ejecución y Validación
        assertThrows(DomainException.class, () -> useCase.listPaymentMethods(userId));
        verify(repository, never()).findByUserId(any());
    }
}
