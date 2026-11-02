package org.jala.edu.user.application.port.out;

import java.util.List;
import org.jala.edu.core.application.port.out.CrudRepositoryPort;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;

public interface UserRepositoryPort extends CrudRepositoryPort<User, UserId> {

    boolean existsByEmail(Email email);

    long countActiveByRole(UserRole role);

    /**
     * @param fragment lower-case text; emails are stored in lower case
     * @return the users whose email contains {@code fragment}, oldest first
     */
    List<User> findByEmailContaining(String fragment);
}
