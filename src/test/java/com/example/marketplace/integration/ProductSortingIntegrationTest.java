package com.example.marketplace.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductSortingIntegrationTest extends AbstractIntegrationTest {

    private record Trio(long bravo30, long charlie10, long alpha20) {
        List<Long> all() {
            return List.of(bravo30, charlie10, alpha20);
        }
    }

    private long createProductWith(String token, long categoryId, String name, String price)
            throws Exception {
        String json = "{\"name\":\"" + name + "\",\"description\":\"d\",\"price\":" + price
                + ",\"categoryId\":" + categoryId + "}";
        return idOf(send(post("/api/products"), token, json).andExpect(status().isCreated()));
    }

    private Trio createTrio() throws Exception {
        String seller = registerAndLogin();
        long categoryId = createCategory(loginAsAdmin());
        long bravo30 = createProductWith(seller, categoryId, "Bravo", "30.00");
        long charlie10 = createProductWith(seller, categoryId, "Charlie", "10.00");
        long alpha20 = createProductWith(seller, categoryId, "Alpha", "20.00");
        return new Trio(bravo30, charlie10, alpha20);
    }

    private List<Long> allIdsInOrder(String sort) throws Exception {
        List<Long> result = new ArrayList<>();
        int page = 0;
        int totalPages;
        do {
            var request = get("/api/products")
                    .param("page", String.valueOf(page))
                    .param("size", "50");
            if (sort != null) {
                request.param("sort", sort);
            }
            String body = send(request, null, null)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<Number> ids = JsonPath.read(body, "$.content[*].id");
            ids.forEach(id -> result.add(id.longValue()));
            totalPages = JsonPath.read(body, "$.totalPages");
            page++;
        } while (page < totalPages);
        return result;
    }

    private List<Long> mineInOrder(String sort, Trio trio) throws Exception {
        return allIdsInOrder(sort).stream().filter(trio.all()::contains).toList();
    }

    @Test
    void sortByPriceAsc_ordersCheapestFirst() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("price,asc", t))
                .containsExactly(t.charlie10(), t.alpha20(), t.bravo30());
    }

    @Test
    void sortByPriceDesc_ordersMostExpensiveFirst() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("price,desc", t))
                .containsExactly(t.bravo30(), t.alpha20(), t.charlie10());
    }

    @Test
    void sortWithoutDirection_defaultsToAsc() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("price", t))
                .containsExactly(t.charlie10(), t.alpha20(), t.bravo30());
    }

    @Test
    void sortByNameAsc_ordersAlphabetically() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("name,asc", t))
                .containsExactly(t.alpha20(), t.bravo30(), t.charlie10());
    }

    @Test
    void sortByNameDesc_ordersReverseAlphabetically() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("name,desc", t))
                .containsExactly(t.charlie10(), t.bravo30(), t.alpha20());
    }

    @Test
    void sortByCreatedAtAsc_ordersOldestFirst() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("createdAt,asc", t))
                .containsExactly(t.bravo30(), t.charlie10(), t.alpha20());
    }

    @Test
    void sortByCreatedAtDesc_ordersNewestFirst() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder("createdAt,desc", t))
                .containsExactly(t.alpha20(), t.charlie10(), t.bravo30());
    }

    @Test
    void withoutSort_ordersNewestFirst() throws Exception {
        Trio t = createTrio();

        assertThat(mineInOrder(null, t))
                .containsExactly(t.alpha20(), t.charlie10(), t.bravo30());
    }

    @Test
    void sort_unknownField_returns400WithDetail() throws Exception {
        send(get("/api/products").param("sort", "password,asc"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Allowed sort fields: price, name, createdAt"));
    }

    @Test
    void sort_unknownDirection_returns400WithDetail() throws Exception {
        send(get("/api/products").param("sort", "price,up"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sort direction must be asc or desc"));
    }

    @Test
    void sort_tooManyParts_returns400WithDetail() throws Exception {
        send(get("/api/products").param("sort", "price,asc,extra"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Invalid sort format. Expected field or field,asc|desc"));
    }

    @Test
    void sort_emptyValue_returns400() throws Exception {
        send(get("/api/products").param("sort", ""), null, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void sort_emptyDirection_returns400() throws Exception {
        send(get("/api/products").param("sort", "price,"), null, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sort direction must be asc or desc"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"price,asc", "price,desc", "name,asc", "name,desc", "createdAt,asc"})
    void sort_pagesDoNotOverlap_evenWithManyEqualValues(String sort) throws Exception {
        createTrio();
        int pageSize = 5;
        List<Long> collected = new ArrayList<>();
        int page = 0;
        int totalPages;
        long total;
        do {
            String body = send(get("/api/products")
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(pageSize))
                    .param("sort", sort), null, null)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            List<Number> ids = JsonPath.read(body, "$.content[*].id");
            ids.forEach(id -> collected.add(id.longValue()));
            totalPages = JsonPath.read(body, "$.totalPages");
            total = ((Number) JsonPath.read(body, "$.totalElement")).longValue();
            page++;
        } while (page < totalPages);

        assertThat(collected).hasSize((int) total);
        assertThat(new HashSet<>(collected)).hasSize((int) total);
    }
}