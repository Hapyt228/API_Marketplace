CREATE TABLE products (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       description VARCHAR(255),
                       price DECIMAL(10, 2) NOT NULL,
                       category_id BIGINT NOT NULL REFERENCES categories(id),
                       seller_id BIGINT NOT NULL REFERENCES users(id),
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
