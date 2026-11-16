CREATE TABLE orders (
    order_id BINARY(16) PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    subtotal DECIMAL(20, 2) NOT NULL,
    status VARCHAR(32) NOT NULL CHECK (status IN (
        'CREATED', 'CONFIRMED', 'READY_FOR_DELIVERY', 'ASSIGNED',
        'ON_THE_WAY', 'DELIVERED', 'CANCELLED'
    )),
    delivery_city VARCHAR(100),
    delivery_avenue VARCHAR(150),
    delivery_house_number VARCHAR(50),
    shipping_cost DECIMAL(20, 2),
    total DECIMAL(20, 2),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
