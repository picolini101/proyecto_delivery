package org.jala.edu.payment.domain.exception;

// Para capturar fallas a la regla de negocio HU CA3/CA4

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
