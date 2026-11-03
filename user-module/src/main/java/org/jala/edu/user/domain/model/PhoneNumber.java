package org.jala.edu.user.domain.model;

import java.util.regex.Pattern;
import org.jala.edu.core.domain.exception.DomainException;

/**
 * Optional contact phone in international E.164 format: {@code +}, country code and number, at most 15 digits and
 * never starting with 0 (e.g. {@code +59170000000}). Surrounding spaces are ignored.
 */
public record PhoneNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    public PhoneNumber {
        if (value == null || !PATTERN.matcher(value.strip()).matches()) {
            throw new DomainException("Invalid phone number: " + value);
        }
        value = value.strip();
    }

    /**
     * @return the phone number, or {@code null} when {@code value} is null or blank (the phone is optional)
     */
    public static PhoneNumber ofNullable(String value) {
        return value == null || value.isBlank() ? null : new PhoneNumber(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
