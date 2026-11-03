package org.jala.edu.user.infrastructure.adapter.out.security;

import org.jala.edu.user.application.port.out.PasswordHasher;
import org.jala.edu.user.domain.model.Password;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * {@link PasswordHasher} with BCrypt (D-10): salted, slow on purpose, and the same hash never repeats.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(Password password) {
        return encoder.encode(password.value());
    }

    @Override
    public boolean matches(Password password, String hash) {
        return hash != null && encoder.matches(password.value(), hash);
    }
}
