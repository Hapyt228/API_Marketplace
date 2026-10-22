package com.example.marketplace.integration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryAuthorizationIntegrationTest
        extends AbstractIntegrationTest {

    private String categoryJson(String name) {
        return """
                {
                  "name": "%s",
                  "description": "Test category"
                }
                """.formatted(name);
    }

    private String uniqueName() {
        return "Category-" + UUID.randomUUID();
    }

    private void assertProblem(
            ResultActions result,
            int expectedStatus,
            String expectedPath
    ) throws Exception {
        result
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(
                        "application/problem+json"
                ))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value(expectedPath));
    }

    @Test
    void anonymous_canReadCategories() throws Exception {
        long categoryId = createCategory(loginAsAdmin());

        send(get("/api/categories"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", instanceOf(java.util.List.class)))
                .andExpect(jsonPath("$[*].id")
                        .value(hasItem((int) categoryId)));

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) categoryId));
    }

    @Test
    void user_canReadCategories() throws Exception {
        long categoryId = createCategory(loginAsAdmin());
        String userToken = registerAndLogin();

        send(get("/api/categories"), userToken, null)
                .andExpect(status().isOk());

        send(
                get("/api/categories/{id}", categoryId),
                userToken,
                null
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) categoryId));
    }

    @Test
    void admin_canReadCategories() throws Exception {
        String adminToken = loginAsAdmin();
        long categoryId = createCategory(adminToken);

        send(get("/api/categories"), adminToken, null)
                .andExpect(status().isOk());

        send(
                get("/api/categories/{id}", categoryId),
                adminToken,
                null
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) categoryId));
    }

    @Test
    void anonymous_cannotCreateCategory() throws Exception {
        String name = uniqueName();

        assertProblem(
                send(
                        post("/api/categories"),
                        null,
                        categoryJson(name)
                ),
                401,
                "/api/categories"
        );

        send(get("/api/categories"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.name == '" + name + "')]"
                ).isEmpty());
    }

    @Test
    void anonymous_cannotUpdateCategory() throws Exception {
        long categoryId = createCategory(loginAsAdmin());

        String originalName = com.jayway.jsonpath.JsonPath.read(
                send(
                        get("/api/categories/{id}", categoryId),
                        null,
                        null
                )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                "$.name"
        );

        assertProblem(
                send(
                        put("/api/categories/{id}", categoryId),
                        null,
                        categoryJson(uniqueName())
                ),
                401,
                "/api/categories/" + categoryId
        );

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(originalName));
    }

    @Test
    void anonymous_cannotDeleteCategory() throws Exception {
        long categoryId = createCategory(loginAsAdmin());

        assertProblem(
                send(
                        delete("/api/categories/{id}", categoryId),
                        null,
                        null
                ),
                401,
                "/api/categories/" + categoryId
        );

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk());
    }

    @Test
    void user_cannotCreateCategory() throws Exception {
        String userToken = registerAndLogin();
        String name = uniqueName();

        assertProblem(
                send(
                        post("/api/categories"),
                        userToken,
                        categoryJson(name)
                ),
                403,
                "/api/categories"
        );

        send(get("/api/categories"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[?(@.name == '" + name + "')]"
                ).isEmpty());
    }

    @Test
    void user_cannotUpdateCategory() throws Exception {
        String adminToken = loginAsAdmin();
        String userToken = registerAndLogin();
        String originalName = uniqueName();

        long categoryId = idOf(
                send(
                        post("/api/categories"),
                        adminToken,
                        categoryJson(originalName)
                ).andExpect(status().isCreated())
        );

        assertProblem(
                send(
                        put("/api/categories/{id}", categoryId),
                        userToken,
                        categoryJson(uniqueName())
                ),
                403,
                "/api/categories/" + categoryId
        );

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(originalName));
    }

    @Test
    void user_cannotDeleteCategory() throws Exception {
        long categoryId = createCategory(loginAsAdmin());
        String userToken = registerAndLogin();

        assertProblem(
                send(
                        delete("/api/categories/{id}", categoryId),
                        userToken,
                        null
                ),
                403,
                "/api/categories/" + categoryId
        );

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) categoryId));
    }

    @Test
    void admin_canCreateCategory() throws Exception {
        String adminToken = loginAsAdmin();
        String name = uniqueName();

        long categoryId = idOf(
                send(
                        post("/api/categories"),
                        adminToken,
                        categoryJson(name)
                )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value(name))
        );

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void admin_canUpdateCategory() throws Exception {
        String adminToken = loginAsAdmin();
        long categoryId = createCategory(adminToken);
        String updatedName = uniqueName();

        send(
                put("/api/categories/{id}", categoryId),
                adminToken,
                categoryJson(updatedName)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName));
    }

    @Test
    void admin_canDeleteCategory() throws Exception {
        String adminToken = loginAsAdmin();
        long categoryId = createCategory(adminToken);

        send(
                delete("/api/categories/{id}", categoryId),
                adminToken,
                null
        )
                .andExpect(status().isOk());

        send(get("/api/categories/{id}", categoryId), null, null)
                .andExpect(status().isNotFound());
    }
}