CREATE TABLE order_status_history (
    order_status_change_id BINARY(16) PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    previous_status VARCHAR(32) NOT NULL CHECK (previous_status IN (
        'CREATED', 'CONFIRMED', 'READY_FOR_DELIVERY', 'ASSIGNED',
        'ON_THE_WAY', 'DELIVERED', 'CANCELLED'
    )),
    new_status VARCHAR(32) NOT NULL CHECK (new_status IN (
        'CREATED', 'CONFIRMED', 'READY_FOR_DELIVERY', 'ASSIGNED',
        'ON_THE_WAY', 'DELIVERED', 'CANCELLED'
    )),
    changed_by_id BINARY(16) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE INDEX idx_order_status_history_order_created
    ON order_status_history(order_id, created_at DESC);
