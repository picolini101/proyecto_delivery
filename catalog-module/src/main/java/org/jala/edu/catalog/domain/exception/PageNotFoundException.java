package org.jala.edu.catalog.domain.exception;

import org.jala.edu.core.domain.exception.NotFoundException;

/**
 * Se lanzará cuando la página solicitada no exista
 */
public class PageNotFoundException extends NotFoundException {

    public PageNotFoundException(int page) {
        super("Page not found: " + page);
    }
}