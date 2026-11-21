package org.jala.edu.catalog.domain.matching;

import java.util.Locale;

/// Coincide si el texto contiene la consulta, sin distinguir mayúsculaRespeta los acentos
public class ContainsIgnoreCaseStrategy implements TextMatchStrategy {

    @Override
    public boolean matches(String candidate, String query) {
        return candidate.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }
}