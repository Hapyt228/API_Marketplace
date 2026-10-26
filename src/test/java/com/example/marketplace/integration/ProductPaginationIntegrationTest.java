package com.example.marketplace.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Comparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductPaginationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void createProducts(int count) throws Exception {
        String seller = registerAndLogin();
        long categoryId = createCategory(loginAsAdmin());
        for (int i = 0; i < count; i++) {
            createProduct(seller, categoryId);
        }
    }

    private long totalElements() throws Exception {
        String body = send(get("/api/products").param("page", "0").param("size", "1"), null, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.totalElement")).longValue();
    }

    @Test
    void getProducts_defaultPaging_returnsPageObjectWithTenItems() throws Exception {
        createProducts(12);

        send(get("/api/products"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElement", greaterThanOrEqualTo(12)))
                .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(2)));
    }

    @Test
    void getProducts_secondPage_returnsRequestedSlice() throws Exception {
        long before = totalElements();
        createProducts(12);
        long total = before + 12;
        int expectedPages = (int) Math.ceil(total / 5.0);

        send(get("/api/products").param("page", "1").param("size", "5"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElement").value((int) total))
                .andExpect(jsonPath("$.totalPages").value(expectedPages));
    }

    @Test
    void getProducts_lastPage_containsRemainder() throws Exception {
        long before = totalElements();
        createProducts(12);
        long total = before + 12;
        int expectedPages = (int) Math.ceil(total / 5.0);
        int lastPageIndex = expectedPages - 1;
        int lastPageSize = (int) (total - 5L * lastPageIndex);

        send(get("/api/products")
                .param("page", String.valueOf(lastPageIndex))
                .param("size", "5"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lastPageSize)))
                .andExpect(jsonPath("$.page").value(lastPageIndex));
    }

    @Test
    void getProducts_pageBeyondRange_returnsEmptyContent() throws Exception {
        createProducts(1);

        send(get("/api/products").param("page", "100000").param("size", "5"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getProducts_sizeAboveMaximum_isLimitedToFifty() throws Exception {
        send(get("/api/products").param("size", "100"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(50));
    }

    @Test
    void getProducts_negativePage_returns400() throws Exception {
        send(get("/api/products").param("page", "-1"), null, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProducts_pageNotANumber_returns400() throws Exception {
        send(get("/api/products").param("page", "abc"), null, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProducts_allPagesTogether_coverEveryProductOnce() throws Exception {
        createProducts(12);
        long total = totalElements();
        int pages = (int) Math.ceil(total / 5.0);
        Set<Integer> uniqueIds = new HashSet<>();
        int collected = 0;

        for (int page = 0; page < pages; page++) {
            String body = send(get("/api/products")
                    .param("page", String.valueOf(page))
                    .param("size", "5"), null, null)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<Integer> ids = JsonPath.read(body, "$.content[*].id");
            collected += ids.size();
            uniqueIds.addAll(ids);
        }

        assertThat(collected).isEqualTo((int) total);
        assertThat(uniqueIds).hasSize((int) total);
    }

    @Test
    void getProducts_items_keepCategorySellerAndRating() throws Exception {
        createProducts(1);

        send(get("/api/products").param("size", "1"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].category").exists())
                .andExpect(jsonPath("$.content[0].seller").exists())
                .andExpect(jsonPath("$.content[0].averageRating").exists())
                .andExpect(jsonPath("$.content[0].reviewsCount").exists());
    }

    @Test
    void getProducts_sizeZero_returns400WithDetail() throws Exception {
        send(get("/api/products").param("size", "0"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Size must be 1 or greater"));
    }

    @Test
    void getProducts_negativeSize_returns400WithDetail() throws Exception {
        send(get("/api/products").param("size", "-5"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Size must be 1 or greater"));
    }

    @Test
    void getProducts_negativePage_returns400WithDetail() throws Exception {
        send(get("/api/products").param("page", "-1"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Page must be 0 or greater"));
    }

    @Test
    void getProducts_sameCreatedAt_pagesDoNotOverlapAndKeepIdOrder() throws Exception {
        createProducts(12);
        jdbcTemplate.update("UPDATE products SET created_at = CURRENT_TIMESTAMP");
        long total = totalElements();
        int pages = (int) Math.ceil(total / 5.0);
        List<Integer> allIds = new ArrayList<>();

        for (int page = 0; page < pages; page++) {
            String body = send(get("/api/products")
                    .param("page", String.valueOf(page))
                    .param("size", "5"), null, null)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<Integer> ids = JsonPath.read(body, "$.content[*].id");
            allIds.addAll(ids);
        }

        assertThat(allIds).hasSize((int) total);
        assertThat(new HashSet<>(allIds)).hasSize((int) total);
        assertThat(allIds).isSortedAccordingTo(Comparator.reverseOrder());
    }

}