CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    subtotal DECIMAL(20,2),
    status VARCHAR(20) NOT NULL CHECK (status IN (
                                                    'CREATED',
                                                    'CONFIRMED',
                                                    'READY_FOR_DELIVERY',
                                                    'ASSIGNED',
                                                    'ON_THE_WAY',
                                                    'DELIVERED',
                                                    'CANCELLED')),
    delivery_address VARCHAR(150),
    shipping_cost DECIMAL(10,2) DEFAULT 10.00,
    total DECIMAL(20,2)
);