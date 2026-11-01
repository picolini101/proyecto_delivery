package org.jala.edu.catalog.domain.exception;

import org.jala.edu.catalog.domain.model.CommerceId;
import org.jala.edu.core.domain.exception.NotFoundException;

/**
 * Se lanza cuando no existe un comercio con el id indicado.
 */
public class CommerceNotFoundException extends NotFoundException {

    public CommerceNotFoundException(CommerceId id) {
        super("Commerce not found: " + id);
    }
}
