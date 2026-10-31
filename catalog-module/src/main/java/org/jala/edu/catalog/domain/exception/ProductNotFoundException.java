package org.jala.edu.catalog.domain.exception;

import org.jala.edu.catalog.domain.model.ProductId;
import org.jala.edu.core.domain.exception.NotFoundException;

/**
 * Se lanza cuando no existe un producto con el id indicado.
 */
public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(ProductId id) {
        super("Product not found: " + id);
    }
}