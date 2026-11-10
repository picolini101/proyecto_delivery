package org.jala.edu.delivery.infrastructure.adapter.out.code;

import java.security.SecureRandom;
import org.jala.edu.delivery.application.port.out.ConfirmationCodeGenerator;
import org.jala.edu.delivery.domain.model.ConfirmationCode;

/**
 * Draws a confirmation code of four digits.
 */
public class RandomConfirmationCodeGenerator implements ConfirmationCodeGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public ConfirmationCode next() {
        return new ConfirmationCode(String.format("%04d", random.nextInt(10_000)));
    }
}
