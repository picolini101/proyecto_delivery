CREATE TABLE failed_payment_log (
                                    failed_payment_id BINARY(16) PRIMARY KEY,
                                    order_id BINARY(16) NOT NULL,
                                    customer_id BINARY(16) NOT NULL,
                                    payment_result VARCHAR(32) NOT NULL CHECK (payment_result IN ('REJECTED')),
                                    reason VARCHAR(500),
                                    created_at TIMESTAMP(6) NOT NULL,
                                    updated_at TIMESTAMP(6) NOT NULL,
                                    CONSTRAINT fk_failed_payment_log_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE INDEX idx_failed_payment_log_order_created
    ON failed_payment_log(order_id, created_at);