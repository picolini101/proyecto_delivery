package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import static org.jala.edu.core.infrastructure.ui.FormBindings.optionalText;

import java.math.BigDecimal;
import javafx.scene.control.TextField;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Convierte el texto de un campo de precio a {@link BigDecimal}.
 * Compartido por los formularios de crear y editar producto (DRY).
 */
final class PriceInput {

    private PriceInput() {
    }

    /**
     * Un campo vacío devuelve {@code null}: el dominio lo rechaza con «Price must be greater than zero».
     *
     * @throws DomainException si el texto escrito no es un número
     */
    static BigDecimal read(TextField field) {
        String text = optionalText(field);
        if (text == null) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new DomainException("Price must be a valid number");
        }
    }
}