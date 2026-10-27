package com.example.marketplace.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductFilterIntegrationTest extends AbstractIntegrationTest {

    private record Fixture(long categoryA, long categoryB) {}

    private long createProductWith(String token, long categoryId, String name, String price)
            throws Exception {
        String json = "{\"name\":\"" + name + "\",\"description\":\"d\",\"price\":" + price
                + ",\"categoryId\":" + categoryId + "}";
        return idOf(send(post("/api/products"), token, json).andExpect(status().isCreated()));
    }

    private Fixture createFixture() throws Exception {
        String seller = registerAndLogin();
        long a = createCategory(loginAsAdmin());
        long b = createCategory(loginAsAdmin());
        createProductWith(seller, a, "Red Phone", "10.00");
        createProductWith(seller, a, "Blue Phone", "20.00");
        createProductWith(seller, a, "Green Laptop", "30.00");
        createProductWith(seller, a, "100% Cotton", "40.00");
        createProductWith(seller, a, "Under_score", "50.00");
        createProductWith(seller, b, "Red Phone", "25.00");
        return new Fixture(a, b);
    }

    private String pageOf(long categoryId, String... keyValues) throws Exception {
        var request = get("/api/products").param("categoryId", String.valueOf(categoryId));
        for (int i = 0; i < keyValues.length; i += 2) {
            request.param(keyValues[i], keyValues[i + 1]);
        }
        return send(request, null, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private List<String> names(String body) {
        return JsonPath.read(body, "$.content[*].name");
    }

    private long total(String body) {
        return ((Number) JsonPath.read(body, "$.totalElement")).longValue();
    }

    @Test
    void categoryId_returnsOnlyProductsOfThatCategory() throws Exception {
        Fixture f = createFixture();

        String bodyA = pageOf(f.categoryA());
        String bodyB = pageOf(f.categoryB());

        assertThat(total(bodyA)).isEqualTo(5);
        assertThat(total(bodyB)).isEqualTo(1);
        assertThat(names(bodyB)).containsExactly("Red Phone");
    }

    @Test
    void categoryId_unknown_returnsEmptyPage() throws Exception {
        send(get("/api/products").param("categoryId", "999999"), null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElement").value(0));
    }

    @Test
    void minPrice_isInclusive() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "minPrice", "30");

        assertThat(names(body)).containsExactlyInAnyOrder("Green Laptop", "100% Cotton", "Under_score");
    }

    @Test
    void maxPrice_isInclusive() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "maxPrice", "20");

        assertThat(names(body)).containsExactlyInAnyOrder("Red Phone", "Blue Phone");
    }

    @Test
    void minAndMaxPrice_returnRange() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "minPrice", "20", "maxPrice", "40");

        assertThat(names(body)).containsExactlyInAnyOrder("Blue Phone", "Green Laptop", "100% Cotton");
        assertThat(total(body)).isEqualTo(3);
    }

    @Test
    void minPriceGreaterThanMaxPrice_returns400() throws Exception {
        send(get("/api/products").param("minPrice", "50").param("maxPrice", "10"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("minPrice must be less than or equal to maxPrice"));
    }

    @Test
    void negativeMinPrice_returns400() throws Exception {
        send(get("/api/products").param("minPrice", "-1"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("minPrice must be greater than or equal to 0"));
    }

    @Test
    void negativeMaxPrice_returns400() throws Exception {
        send(get("/api/products").param("maxPrice", "-1"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("maxPrice must be greater than or equal to 0"));
    }

    @Test
    void priceNotANumber_returns400() throws Exception {
        send(get("/api/products").param("minPrice", "abc"), null, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_isCaseInsensitive() throws Exception {
        Fixture f = createFixture();

        assertThat(names(pageOf(f.categoryA(), "q", "phone")))
                .containsExactlyInAnyOrder("Red Phone", "Blue Phone");
        assertThat(names(pageOf(f.categoryA(), "q", "PHONE")))
                .containsExactlyInAnyOrder("Red Phone", "Blue Phone");
    }

    @Test
    void search_percentSign_isTreatedAsPlainText() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "q", "%");

        assertThat(names(body)).containsExactly("100% Cotton");
    }

    @Test
    void search_underscore_isTreatedAsPlainText() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "q", "_");

        assertThat(names(body)).containsExactly("Under_score");
    }

    @Test
    void search_blank_isIgnored() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "q", "   ");

        assertThat(total(body)).isEqualTo(5);
    }

    @Test
    void search_noMatch_returnsEmptyPage() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "q", "zzz-no-such-product");

        assertThat(names(body)).isEmpty();
        assertThat(total(body)).isZero();
    }

    @Test
    void filters_canBeCombined() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "q", "phone", "maxPrice", "10");

        assertThat(names(body)).containsExactly("Red Phone");
        assertThat(total(body)).isEqualTo(1);
    }

    @Test
    void filter_withPagination_countsOnlyFilteredProducts() throws Exception {
        Fixture f = createFixture();

        String page1 = pageOf(f.categoryA(), "size", "2", "page", "1");
        String page2 = pageOf(f.categoryA(), "size", "2", "page", "2");

        assertThat(names(page1)).hasSize(2);
        assertThat(total(page1)).isEqualTo(5);
        assertThat((Integer) JsonPath.read(page1, "$.totalPages")).isEqualTo(3);
        assertThat(names(page2)).hasSize(1);
    }

    @Test
    void filter_withSort_ordersFilteredProducts() throws Exception {
        Fixture f = createFixture();

        String body = pageOf(f.categoryA(), "sort", "price,desc");

        assertThat(names(body)).containsExactly(
                "Under_score", "100% Cotton", "Green Laptop", "Blue Phone", "Red Phone");
    }
}