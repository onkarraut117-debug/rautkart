package in.rautkart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Catalogue browsing. The empty-search case is a regression test: a null bind
 * parameter inside CONCAT made PostgreSQL fail type inference and 500 the
 * default shop page.
 */
class CatalogApiTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("the default shop page returns the whole catalogue")
    void searchWithNoFiltersWorks() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(59))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("a search term narrows the results")
    void searchByTerm() throws Exception {
        mvc.perform(get("/api/products").param("q", "dal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(7));
    }

    @Test
    @DisplayName("LIKE wildcards in the search term are escaped, not honoured")
    void wildcardsAreEscaped() throws Exception {
        // Without escaping these would match every row.
        mvc.perform(get("/api/products").param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/api/products").param("q", "_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("filters combine: category plus max price plus sort")
    void filtersCombine() throws Exception {
        mvc.perform(get("/api/products")
                        .param("category", "dals-pulses")
                        .param("maxPrice", "120")
                        .param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].price").value(98.00));
    }

    @Test
    @DisplayName("an unknown category yields an empty page rather than an error")
    void unknownCategoryIsEmpty() throws Exception {
        mvc.perform(get("/api/products").param("category", "does-not-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("product detail is served by slug and carries derived fields")
    void productDetailBySlug() throws Exception {
        mvc.perform(get("/api/products/india-gate-basmati-rice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("India Gate Basmati Rice"))
                .andExpect(jsonPath("$.unit").value("1 kg"))
                // Derived server-side so both clients agree: 189 off 225 is 16%.
                .andExpect(jsonPath("$.discountPercent").value(16))
                .andExpect(jsonPath("$.inStock").value(true))
                .andExpect(jsonPath("$.availabilityLabel").value("In stock"));
    }

    @Test
    @DisplayName("a missing slug is a 404, not a 500")
    void unknownSlugIsNotFound() throws Exception {
        mvc.perform(get("/api/products/no-such-product"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("low stock is reported by the server, not computed by the client")
    void lowStockLabelComesFromTheServer() throws Exception {
        setStock("paneer-fresh", 4);

        mvc.perform(get("/api/products/paneer-fresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").value(true))
                .andExpect(jsonPath("$.availabilityLabel").value("Only 4 left in stock"));
    }

    @Test
    @DisplayName("categories come back in display order")
    void categoriesAreSorted() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].slug").value("grains-rice"));
    }
}
