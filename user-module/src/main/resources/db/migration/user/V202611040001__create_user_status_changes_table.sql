-- #82: history of user activations and deactivations (FUN-003). Rows are never updated.
CREATE TABLE user_status_changes
(
    id              VARCHAR(36)  NOT NULL,
    user_id         VARCHAR(36)  NOT NULL,
    previous_status VARCHAR(20)  NOT NULL,
    new_status      VARCHAR(20)  NOT NULL,
    changed_by_id   VARCHAR(36)  NOT NULL,
    changed_by_name VARCHAR(201) NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    CONSTRAINT pk_user_status_changes PRIMARY KEY (id),
    CONSTRAINT fk_user_status_changes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX ix_user_status_changes_user ON user_status_changes (user_id, created_at);
