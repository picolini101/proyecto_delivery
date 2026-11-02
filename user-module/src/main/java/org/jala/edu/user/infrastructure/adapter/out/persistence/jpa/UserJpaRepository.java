package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;

interface UserJpaRepository extends BaseEntityRepository<UserJpaEntity, String> {

    boolean existsByEmail(String email);

    long countByRoleAndStatus(String role, String status);

    List<UserJpaEntity> findByEmailContainingOrderByCreatedAtAsc(String fragment);
}
