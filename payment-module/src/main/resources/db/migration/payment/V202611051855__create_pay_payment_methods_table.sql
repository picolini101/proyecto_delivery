CREATE TABLE pay_payment_methods (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    masked_number VARCHAR(20) NOT NULL,
    is_last_registered BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pay_payment_methods PRIMARY KEY (id)
);