CREATE TABLE order_line (
    order_line_id BINARY(16) PRIMARY KEY,
    product_id BINARY(16) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(20, 2) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    commerce_id BINARY(16) NOT NULL,
    subtotal DECIMAL(20, 2) NOT NULL,
    order_id BINARY(16) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_order_line_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
