package org.jala.edu.payment.domain.validation;

import java.math.BigDecimal;

import org.jala.edu.core.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {

    @Test
    void shouldAcceptValidAmount() {
        BigDecimal result = AmountValidator.validate(
                new BigDecimal("100.50"));

        assertEquals(new BigDecimal("100.50"), result);
    }

    @Test
    void shouldRejectZeroAmount() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> AmountValidator.validate(
                        new BigDecimal("0.00")));

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage());
    }

    @Test
    void shouldRejectNegativeAmount() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> AmountValidator.validate(
                        new BigDecimal("-15.00")));

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage());
    }

    @Test
    void shouldRejectNegativeMinimumAmount() {
        assertThrows(
                DomainException.class,
                () -> AmountValidator.validate(
                        new BigDecimal("-0.01")));
    }

    @Test
    void shouldAcceptMinimumAmount() {
        BigDecimal result = AmountValidator.validate(
                new BigDecimal("0.01"));

        assertEquals(new BigDecimal("0.01"), result);
    }

    @Test
    void shouldAcceptMaximumAmount() {
        BigDecimal result = AmountValidator.validate(
                new BigDecimal("5000.00"));

        assertEquals(new BigDecimal("5000.00"), result);
    }

    @Test
    void shouldRoundAmountToTwoDecimals() {
        BigDecimal result = AmountValidator.validate(
                new BigDecimal("100.505"));

        assertEquals(new BigDecimal("100.51"), result);
    }

    @Test
    void shouldRoundAmountDownToTwoDecimals() {
        BigDecimal result = AmountValidator.validate(
                new BigDecimal("100.504"));

        assertEquals(new BigDecimal("100.50"), result);
    }

    @Test
    void shouldRejectAmountRoundedToZero() {
        assertThrows(
                DomainException.class,
                () -> AmountValidator.validate(
                        new BigDecimal("0.004")));
    }

    @Test
    void shouldRejectNullAmount() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> AmountValidator.validate(null));

        assertEquals(
                "El monto del pago debe ser mayor a cero",
                exception.getMessage());
    }
}
