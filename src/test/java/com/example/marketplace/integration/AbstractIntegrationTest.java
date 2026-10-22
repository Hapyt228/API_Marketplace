package com.example.marketplace.integration;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "test-secret-test-secret-test-secret-123456");
        registry.add("jwt.expiration-ms", () -> "3600000");
        registry.add("app.admin.email", () -> "admin@test.com");
        registry.add("app.admin.password", () -> "AdminPassword123");
    }

    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions send(MockHttpServletRequestBuilder builder, String token, String body)
            throws Exception {
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(builder);
    }

    protected String registerAndLogin() throws Exception {
        String email = "user" + UUID.randomUUID() + "@test.com";
        String json = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        send(post("/api/auth/register"), null, json).andExpect(status().isCreated());
        String response = send(post("/api/auth/login"), null, json)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    protected long idOf(ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    protected long createCategory(String token) throws Exception {
        return idOf(send(post("/api/categories"), token,
                "{\"name\":\"Cat" + UUID.randomUUID() + "\",\"description\":\"d\"}")
                .andExpect(status().isCreated()));
    }

    protected long createProduct(String token, long categoryId) throws Exception {
        return idOf(send(post("/api/products"), token,
                "{\"name\":\"Phone\",\"description\":\"d\",\"price\":100.00,\"categoryId\":"
                        + categoryId + "}")
                .andExpect(status().isCreated()));
    }

    protected String cartJson(long productId, int quantity) {
        return "{\"productId\":" + productId + ",\"quantity\":" + quantity + "}";
    }

    protected String loginAsAdmin() throws Exception {
        String json = """
            {
              "email": "admin@test.com",
              "password": "AdminPassword123"
            }
            """;

        String response = send(post("/api/auth/login"), null, json)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }
}