package org.jala.edu.delivery.application.port.out;

import org.jala.edu.delivery.domain.model.ConfirmationCode;

/**
 * Creates the code the customer will read to the courier at the door.
 */
public interface ConfirmationCodeGenerator {

    ConfirmationCode next();
}
