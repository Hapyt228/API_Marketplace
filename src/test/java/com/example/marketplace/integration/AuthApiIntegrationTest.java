package com.example.marketplace.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiIntegrationTest extends AbstractIntegrationTest {

    private String json(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = json("dup" + UUID.randomUUID() + "@test.com", "password123");
        send(post("/api/auth/register"), null, body).andExpect(status().isCreated());

        send(post("/api/auth/register"), null, body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email already exists"));
    }

    @Test
    void register_invalidEmail_returns400WithFieldError() throws Exception {
        send(post("/api/auth/register"), null, json("not-an-email", "password123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        send(post("/api/auth/register"), null, json("short" + UUID.randomUUID() + "@test.com", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    @Test
    void login_wrongPasswordAndUnknownEmail_returnSame401() throws Exception {
        String email = "login" + UUID.randomUUID() + "@test.com";
        send(post("/api/auth/register"), null, json(email, "password123"))
                .andExpect(status().isCreated());

        send(post("/api/auth/login"), null, json(email, "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));

        send(post("/api/auth/login"), null, json("nobody" + UUID.randomUUID() + "@test.com", "password123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void protectedEndpoint_withoutToken_returns401Json() throws Exception {
        send(get("/api/cart"), null, null)
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void protectedEndpoint_withGarbageToken_returns401() throws Exception {
        send(get("/api/cart"), "garbage.token.value", null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void catalog_isPublic() throws Exception {
        send(get("/api/products"), null, null).andExpect(status().isOk());
        send(get("/api/categories"), null, null).andExpect(status().isOk());
    }
}