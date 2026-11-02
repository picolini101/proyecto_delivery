package org.jala.edu.user.application.port.out;

import org.jala.edu.user.domain.model.Password;

/**
 * One-way password hashing (D-10). The application only handles hashes; the algorithm lives in an adapter.
 */
public interface PasswordHasher {

    String hash(Password password);

    boolean matches(Password password, String hash);
}
