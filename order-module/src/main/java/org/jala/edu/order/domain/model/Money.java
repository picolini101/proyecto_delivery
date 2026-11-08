package org.jala.edu.order.domain.model;

import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.order.domain.exception.InvalidMoneyValueException;
import org.jala.edu.order.domain.exception.InvalidQuantityValueException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal value) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money{
        Objects.requireNonNull(value, "Money value can't be null");
        if (value.compareTo(BigDecimal.ZERO) < 0){
            throw new InvalidMoneyValueException(value);
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal value){
        return new Money(value);
    }

    public static Money of(double value){
        return new Money(BigDecimal.valueOf(value));
    }

    public Money add(Money other){
        Objects.requireNonNull(other, "Can't add be nulll");
        return new Money(this.value.add(other.value));
    }

    public Money multiply(Quantity quantity) {
        Objects.requireNonNull(quantity, "Quantity no puede ser nula");
        return new Money(this.value.multiply(BigDecimal.valueOf(quantity.value())));
    }
    public Money multiply(int quantity) {
        return multiply(Quantity.of(quantity));
    }

    @Override
    public String toString(){
        return value.toString();
    }
}
