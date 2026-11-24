package org.jala.edu.catalog.infrastructure.adapter.in.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.jala.edu.catalog.application.dto.CommerceSummary;

/**
 * Lógica pura del selector de comercio de la gestión de productos (sin JavaFX, fácil de probar).
 * La opción «Todos los comercios» es un {@link CommerceSummary} sin id, para reutilizar
 * {@link CommerceNameConverter}.
 */
final class CommerceFilterOptions {

    /** Opción por defecto: no filtra por comercio (su id es {@code null}). */
    static final CommerceSummary ALL =
            new CommerceSummary(null, "Todos los comercios", null, null, null, 0L);

    private CommerceFilterOptions() {
    }

    /** Opciones del selector: «Todos los comercios» primero y luego los comercios registrados. */
    static List<CommerceSummary> options(List<CommerceSummary> commerces) {
        List<CommerceSummary> options = new ArrayList<>();
        options.add(ALL);
        options.addAll(commerces);
        return options;
    }

    /** Id con el que se filtra: el de la opción elegida, o {@code null} si no hay filtro. */
    static UUID idOf(CommerceSummary option) {
        return option == null ? null : option.id();
    }

    /**
     * Mantiene el comercio pedido mientras siga registrado; si fue eliminado, vuelve a
     * «Todos los comercios» ({@code null}).
     */
    static UUID resolveId(List<CommerceSummary> commerces, UUID requestedId) {
        boolean stillExists = requestedId != null
                && commerces.stream().anyMatch(commerce -> requestedId.equals(commerce.id()));
        return stillExists ? requestedId : null;
    }

    /** Opción que corresponde al id; con {@code null} devuelve «Todos los comercios». */
    static CommerceSummary optionFor(List<CommerceSummary> options, UUID id) {
        return options.stream()
                .filter(option -> Objects.equals(option.id(), id))
                .findFirst()
                .orElse(ALL);
    }
}