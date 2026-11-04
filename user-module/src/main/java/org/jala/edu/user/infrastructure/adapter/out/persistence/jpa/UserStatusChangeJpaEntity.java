package org.jala.edu.user.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jala.edu.core.infrastructure.persistence.jpa.BaseJpaEntity;

@Entity
@Table(name = "user_status_changes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatusChangeJpaEntity extends BaseJpaEntity {

    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String userId;

    @Column(name = "previous_status", length = 20, nullable = false, updatable = false)
    private String previousStatus;

    @Column(name = "new_status", length = 20, nullable = false, updatable = false)
    private String newStatus;

    @Column(name = "changed_by_id", length = 36, nullable = false, updatable = false)
    private String changedById;

    @Column(name = "changed_by_name", length = 201, nullable = false, updatable = false)
    private String changedByName;

    public UserStatusChangeJpaEntity(String id, String userId, String previousStatus, String newStatus,
                                     String changedById, String changedByName, Instant createdAt) {
        super(createdAt, createdAt);
        this.id = id;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedById = changedById;
        this.changedByName = changedByName;
    }
}
