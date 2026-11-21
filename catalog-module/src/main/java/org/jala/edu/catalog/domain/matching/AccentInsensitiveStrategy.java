package org.jala.edu.catalog.domain.matching;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class AccentInsensitiveStrategy implements TextMatchStrategy {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    @Override
    public boolean matches(String candidate, String query) {
        return normalize(candidate).contains(normalize(query));
    }

    private static String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return DIACRITICS.matcher(decomposed).replaceAll("").toLowerCase(Locale.ROOT);
    }
}