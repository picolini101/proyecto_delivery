-- #33: historial de transacciones de pago y prevencion de doble cobro (FUN-003).
CREATE TABLE pay_payment_transactions
(
    id                VARCHAR(36)    NOT NULL,
    order_id          VARCHAR(36)    NOT NULL,
    user_id           VARCHAR(36)    NOT NULL,
    payment_method_id VARCHAR(36)    NOT NULL,
    amount            DECIMAL(12, 2) NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    reason            VARCHAR(255)   NOT NULL,
    created_at        DATETIME(6)    NOT NULL,
    CONSTRAINT pk_pay_payment_transactions PRIMARY KEY (id)
);
CREATE INDEX ix_pay_payment_transactions_user ON pay_payment_transactions (user_id, created_at);
CREATE INDEX ix_pay_payment_transactions_order ON pay_payment_transactions (order_id, status);