package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import org.jala.edu.core.infrastructure.config.ConditionalOnJpaPersistence;
import org.jala.edu.core.infrastructure.persistence.jpa.JpaCrudRepositoryAdapter;
import org.jala.edu.user.application.port.out.UserRepositoryPort;
import org.jala.edu.user.domain.model.Email;
import org.jala.edu.user.domain.model.User;
import org.jala.edu.user.domain.model.UserId;
import org.jala.edu.user.domain.model.UserRole;
import org.jala.edu.user.domain.model.UserStatus;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnJpaPersistence
class UserPersistenceAdapter extends JpaCrudRepositoryAdapter<User, UserId, UserJpaEntity, String>
        implements UserRepositoryPort {

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    UserPersistenceAdapter(UserJpaRepository repository) {
        super(repository, new UserPersistenceMapper());
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public long countActiveByRole(UserRole role) {
        return repository.countByRoleAndStatus(role.name(), UserStatus.ACTIVE.name());
    }

    @Override
    public List<User> findByEmailContaining(String fragment) {
        return repository.findByEmailContainingOrderByCreatedAtAsc(fragment).stream().map(mapper::toDomain).toList();
    }
}
