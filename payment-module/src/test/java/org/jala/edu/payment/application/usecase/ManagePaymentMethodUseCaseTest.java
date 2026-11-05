package org.jala.edu.payment.application.usecase;

import org.jala.edu.core.contract.UserRole;
import org.jala.edu.core.contract.UserService;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.payment.application.port.in.RegisterPaymentMethodCommand;
import org.jala.edu.payment.application.port.out.PaymentMethodRepositoryPort;
import org.jala.edu.payment.domain.model.PaymentMethod;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        // Arrange
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Mi Tarjeta Principal", "TARJETA", "4532123456789012"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);
        when(repository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PaymentMethod result = useCase.registerPaymentMethod(command);

        // Assert
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
        // Arrange
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Alias Valido", "TARJETA", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(false);

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, () ->
                useCase.registerPaymentMethod(command)
        );

        assertEquals("User not authorized for this operation", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA3: Debe lanzar DomainException cuando el tipo de pago es inválido")
    void shouldThrowExceptionWhenPaymentTypeIsInvalid() {
        // Arrange
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "Alias Valido", "PAYPAL_INVALIDO", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, () ->
                useCase.registerPaymentMethod(command)
        );

        assertTrue(exception.getMessage().contains("Tipo de pago inválido"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA3: Debe lanzar DomainException cuando el alias es nulo o vacío")
    void shouldThrowExceptionWhenAliasIsEmpty() {
        // Arrange
        RegisterPaymentMethodCommand command = new RegisterPaymentMethodCommand(
                userId, "  ", "TRANSFERENCIA", "1234"
        );
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);

        // Act & Assert
        assertThrows(DomainException.class, () -> useCase.registerPaymentMethod(command));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CA4: Debe listar métodos de pago cuando el usuario es CUSTOMER autorizado")
    void shouldListPaymentMethodsSuccessfully() {
        // Arrange
        when(userService.isActiveWithRole(userId, UserRole.CUSTOMER)).thenReturn(true);
        when(repository.findByUserId(userId)).thenReturn(List.of());

        // Act
        List<PaymentMethod> result = useCase.listPaymentMethods(userId);

        // Assert
        assertNotNull(result);
        verify(repository).findByUserId(userId);
    }
}