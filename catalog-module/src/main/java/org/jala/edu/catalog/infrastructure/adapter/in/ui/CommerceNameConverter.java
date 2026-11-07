package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import javafx.util.StringConverter;
import org.jala.edu.catalog.application.dto.CommerceSummary;

/** Muestra el nombre del comercio en los ComboBox de comercios. */
final class CommerceNameConverter extends StringConverter<CommerceSummary> {

    @Override
    public String toString(CommerceSummary commerce) {
        return commerce == null ? "" : commerce.name();
    }

    @Override
    public CommerceSummary fromString(String text) {
        return null;
    }
}