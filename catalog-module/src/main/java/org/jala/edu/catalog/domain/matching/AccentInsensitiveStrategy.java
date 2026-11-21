package org.jala.edu.catalog.domain.matching;

import java.text.Normalizer;
import java.util.Locale;

public class AccentInsensitiveStrategy implements TextMatchStrategy {

    @Override
    public boolean matches(String candidate, String query) {
        return normalize(candidate).contains(normalize(query));
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}