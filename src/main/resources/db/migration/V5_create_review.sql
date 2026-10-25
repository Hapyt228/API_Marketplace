CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL REFERENCES users(id),
                         product_id BIGINT NOT NULL REFERENCES products(id),
                         rating INTEGER NOT NULL,
                         text VARCHAR(255),
                         created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_reviews_rating
                             CHECK (rating BETWEEN 1 AND 5),

                         CONSTRAINT uk_reviews_user_product
                             UNIQUE (user_id, product_id)
);