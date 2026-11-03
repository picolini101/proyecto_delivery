package org.jala.edu.user.infrastructure.adapter.out.persistence.memory;

import java.util.List;
import java.util.Optional;
import org.jala.edu.core.infrastructure.config.ConditionalOnInMemoryPersistence;
import org.jala.edu.core.infrastructure.persistence.memory.InMemoryCrudRepository;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;
import org.springframework.stereotype.Component;

/**
 * Stores users in memory. Active when {@code delivery.persistence.type=memory};
 * data is lost when the application stops.
 */
@Component
@ConditionalOnInMemoryPersistence
public class InMemoryUserRepositoryAdapter extends InMemoryCrudRepository<User, UserId> implements UserRepositoryPort {

    @Override
    public boolean existsByEmail(Email email) {
        return stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public long countActiveByRole(UserRole role) {
        return stream().filter(user -> user.isActive() && user.getRole() == role).count();
    }

    @Override
    public List<User> findByEmailContaining(String fragment) {
        return findAll().stream().filter(user -> user.getEmail().value().contains(fragment)).toList();
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }
}
