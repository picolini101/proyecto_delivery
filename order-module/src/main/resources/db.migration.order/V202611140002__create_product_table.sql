CREATE TABLE products (
        product_id BIGINT PRIMARY KEY,
        product_name VARCHAR(100),
        unit_price DOUBLE,
        quantity INT,
        merchant_id INT,
        order_id INT,
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
);