package org.jala.edu.order.domain.exception;

import java.util.UUID;
import org.jala.edu.core.domain.exception.DomainException;

public class ProductNotFoundInCartException extends DomainException {

    public ProductNotFoundInCartException(String message) {
        super(message);
    }

    public static ProductNotFoundInCartException of(UUID productId) {
        return new ProductNotFoundInCartException("The product with ID " + productId + " not found in Car");
    }
}
