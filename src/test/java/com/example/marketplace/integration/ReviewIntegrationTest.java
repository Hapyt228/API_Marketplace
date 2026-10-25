package com.example.marketplace.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewIntegrationTest extends AbstractIntegrationTest {

    private String reviewJson(int rating) {
        return "{\"rating\":" + rating + ",\"text\":\"Good product\"}";
    }

    private long newProduct(String sellerToken) throws Exception {
        return createProduct(sellerToken, createCategory(loginAsAdmin()));
    }

    private long addReview(String token, long productId, int rating) throws Exception {
        return idOf(send(post("/api/products/{id}/reviews", productId), token, reviewJson(rating))
                .andExpect(status().isCreated()));
    }

    @Test
    void createReview_returns201_andAppearsInPublicList() throws Exception {
        String seller = registerAndLogin();
        String buyer = registerAndLogin();
        long productId = newProduct(seller);

        send(post("/api/products/{id}/reviews", productId), buyer, reviewJson(5))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.productId").value((int) productId));

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void createReview_withoutToken_returns401() throws Exception {
        long productId = newProduct(registerAndLogin());

        send(post("/api/products/{id}/reviews", productId), null, reviewJson(5))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReview_duplicate_returns409() throws Exception {
        long productId = newProduct(registerAndLogin());
        String buyer = registerAndLogin();
        addReview(buyer, productId, 4);

        send(post("/api/products/{id}/reviews", productId), buyer, reviewJson(5))
                .andExpect(status().isConflict());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createReview_ratingOutOfRange_returns400() throws Exception {
        long productId = newProduct(registerAndLogin());
        String buyer = registerAndLogin();

        send(post("/api/products/{id}/reviews", productId), buyer, reviewJson(0))
                .andExpect(status().isBadRequest());
        send(post("/api/products/{id}/reviews", productId), buyer, reviewJson(6))
                .andExpect(status().isBadRequest());
        send(post("/api/products/{id}/reviews", productId), buyer, "{\"text\":\"no rating\"}")
                .andExpect(status().isBadRequest());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createReview_unknownProduct_returns404() throws Exception {
        String buyer = registerAndLogin();

        send(post("/api/products/{id}/reviews", 999999L), buyer, reviewJson(5))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReviews_unknownProduct_returns404() throws Exception {
        send(get("/api/products/{id}/reviews", 999999L), null, null)
                .andExpect(status().isNotFound());
    }

    @Test
    void seller_cannotReviewOwnProduct_returns403() throws Exception {
        String seller = registerAndLogin();
        long productId = newProduct(seller);

        send(post("/api/products/{id}/reviews", productId), seller, reviewJson(5))
                .andExpect(status().isForbidden());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void author_canDeleteOwnReview_returns204() throws Exception {
        long productId = newProduct(registerAndLogin());
        String buyer = registerAndLogin();
        long reviewId = addReview(buyer, productId, 5);

        send(delete("/api/reviews/{id}", reviewId), buyer, null)
                .andExpect(status().isNoContent());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void otherUser_cannotDeleteForeignReview_returns403() throws Exception {
        long productId = newProduct(registerAndLogin());
        String author = registerAndLogin();
        String stranger = registerAndLogin();
        long reviewId = addReview(author, productId, 5);

        send(delete("/api/reviews/{id}", reviewId), stranger, null)
                .andExpect(status().isForbidden());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void admin_canDeleteAnyReview_returns204() throws Exception {
        long productId = newProduct(registerAndLogin());
        String author = registerAndLogin();
        long reviewId = addReview(author, productId, 3);

        send(delete("/api/reviews/{id}", reviewId), loginAsAdmin(), null)
                .andExpect(status().isNoContent());

        send(get("/api/products/{id}/reviews", productId), null, null)
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteReview_withoutToken_returns401() throws Exception {
        long productId = newProduct(registerAndLogin());
        long reviewId = addReview(registerAndLogin(), productId, 5);

        send(delete("/api/reviews/{id}", reviewId), null, null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteReview_unknownId_returns404() throws Exception {
        send(delete("/api/reviews/{id}", 999999L), registerAndLogin(), null)
                .andExpect(status().isNotFound());
    }

    @Test
    void product_withoutReviews_hasZeroRatingAndCount() throws Exception {
        long productId = newProduct(registerAndLogin());

        send(get("/api/products/{id}", productId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(0.0))
                .andExpect(jsonPath("$.reviewsCount").value(0));
    }

    @Test
    void product_withTwoReviews_showsAverageRatingAndCount() throws Exception {
        long productId = newProduct(registerAndLogin());
        addReview(registerAndLogin(), productId, 5);
        addReview(registerAndLogin(), productId, 2);

        send(get("/api/products/{id}", productId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(3.5))
                .andExpect(jsonPath("$.reviewsCount").value(2));
    }
}