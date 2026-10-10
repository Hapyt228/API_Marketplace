CREATE TABLE carts (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE REFERENCES users(id)
);

CREATE TABLE cart_items (
                         id BIGSERIAL PRIMARY KEY,
                         cart_id BIGINT NOT NULL REFERENCES carts(id),
                         product_id BIGINT NOT NULL REFERENCES products(id),
                         quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        total_price DECIMAL(10, 2) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
                        id BIGSERIAL PRIMARY KEY,
                        order_id BIGINT NOT NULL REFERENCES orders(id),
                        product_id BIGINT NOT NULL REFERENCES products(id),
                        quantity INTEGER NOT NULL CHECK (quantity > 0),
                        price_at_purchase DECIMAL(10, 2) NOT NULL
);