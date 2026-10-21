package com.example.marketplace.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketplaceFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void fullFlow_cartToOrder_clearsCart() throws Exception {
        String seller = registerAndLogin();
        String buyer = registerAndLogin();
        long productId = createProduct(seller, createCategory(seller));

        send(post("/api/cart"), buyer, cartJson(productId, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalPrice").value(200.0));

        send(post("/api/orders"), buyer, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalPrice").value(200.0));

        send(get("/api/cart"), buyer, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        send(get("/api/orders"), buyer, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createOrder_emptyCart_returns400() throws Exception {
        String buyer = registerAndLogin();

        send(post("/api/orders"), buyer, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Cart is empty"));
    }

    @Test
    void addToCart_negativeQuantity_returns400() throws Exception {
        String buyer = registerAndLogin();

        send(post("/api/cart"), buyer, cartJson(1L, -1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("quantity"));
    }

    @Test
    void addToCart_unknownProduct_returns404() throws Exception {
        String buyer = registerAndLogin();

        send(post("/api/cart"), buyer, cartJson(999999L, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToCart_malformedJson_returns400() throws Exception {
        String buyer = registerAndLogin();

        send(post("/api/cart"), buyer, "{bad json")
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProduct_unknownId_returns404() throws Exception {
        send(get("/api/products/999999"), null, null).andExpect(status().isNotFound());
    }

    @Test
    void getProduct_idNotANumber_returns400() throws Exception {
        send(get("/api/products/abc"), null, null).andExpect(status().isBadRequest());
    }

    @Test
    void otherUser_cannotUpdateOrDeleteProduct() throws Exception {
        String seller = registerAndLogin();
        String stranger = registerAndLogin();
        long categoryId = createCategory(seller);
        long productId = createProduct(seller, categoryId);
        String update = "{\"name\":\"Hacked\",\"description\":\"d\",\"price\":1.00,\"categoryId\":"
                + categoryId + "}";

        send(put("/api/products/" + productId), stranger, update)
                .andExpect(status().isForbidden());
        send(delete("/api/products/" + productId), stranger, null)
                .andExpect(status().isForbidden());
    }

    @Test
    void otherUser_cannotSeeForeignOrder() throws Exception {
        String seller = registerAndLogin();
        String buyer = registerAndLogin();
        String stranger = registerAndLogin();
        long productId = createProduct(seller, createCategory(seller));
        send(post("/api/cart"), buyer, cartJson(productId, 1)).andExpect(status().isCreated());
        long orderId = idOf(send(post("/api/orders"), buyer, null).andExpect(status().isCreated()));

        send(get("/api/orders/" + orderId), stranger, null).andExpect(status().isForbidden());
        send(get("/api/orders/" + orderId), buyer, null).andExpect(status().isOk());
    }

    @Test
    void deleteProduct_alreadyOrdered_returns409() throws Exception {
        String seller = registerAndLogin();
        String buyer = registerAndLogin();
        long productId = createProduct(seller, createCategory(seller));
        send(post("/api/cart"), buyer, cartJson(productId, 1)).andExpect(status().isCreated());
        send(post("/api/orders"), buyer, null).andExpect(status().isCreated());

        send(delete("/api/products/" + productId), seller, null)
                .andExpect(status().isConflict());
    }
}