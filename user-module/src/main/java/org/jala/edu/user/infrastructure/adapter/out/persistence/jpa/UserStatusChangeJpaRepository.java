package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import java.util.List;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseEntityRepository;

interface UserStatusChangeJpaRepository extends BaseEntityRepository<UserStatusChangeJpaEntity, String> {

    List<UserStatusChangeJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);
}
