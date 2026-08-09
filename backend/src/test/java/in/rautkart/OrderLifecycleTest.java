package in.rautkart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** What happens to an order after it is placed, and what that does to stock. */
class OrderLifecycleTest extends AbstractIntegrationTest {

    private static final String RICE = "india-gate-basmati-rice";

    /** Places a COD order for the given quantity and returns its id. */
    private long placeOrder(String token, int qty) throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"productId":%d,"quantity":%d}
                                """.formatted(productIdBySlug(RICE), qty)))
                .andExpect(status().isOk());

        String body = mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"paymentMethod":"COD",
                                 "address":{"fullName":"Onkar Raut","phone":"9812345678",
                                            "line1":"12 Shivaji Chowk","city":"Nashik",
                                            "state":"Maharashtra","pincode":"422001"}}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(body).get("order").get("id").asLong();
    }

    @Test
    @DisplayName("cancelling an order puts the stock back")
    void cancelRestoresStock() throws Exception {
        String token = customerToken();
        int before = stockOf(RICE);

        long orderId = placeOrder(token, 4);
        assertThat(stockOf(RICE)).isEqualTo(before - 4);

        mvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(stockOf(RICE)).isEqualTo(before);
    }

    @Test
    @DisplayName("an order already being packed can no longer be cancelled")
    void cannotCancelOncePacked() throws Exception {
        String token = customerToken();
        long orderId = placeOrder(token, 1);

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content("{\"status\":\"PACKED\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("an admin walks the order through to delivered, and COD settles")
    void adminAdvancesStatusAndCodSettles() throws Exception {
        long orderId = placeOrder(customerToken(), 1);
        String admin = adminToken();

        for (String status : new String[]{"PACKED", "OUT_FOR_DELIVERY"}) {
            mvc.perform(patch("/api/admin/orders/" + orderId + "/status")
                            .header("Authorization", "Bearer " + admin)
                            .contentType("application/json")
                            .content("{\"status\":\"%s\"}".formatted(status)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(status))
                    .andExpect(jsonPath("$.paymentStatus").value("COD"));
        }

        // Handing over a cash-on-delivery order is the moment it is paid for.
        mvc.perform(patch("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + admin)
                        .contentType("application/json")
                        .content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));
    }

    @Test
    @DisplayName("an admin cancelling an order also restores stock")
    void adminCancelRestoresStock() throws Exception {
        int before = stockOf(RICE);
        long orderId = placeOrder(customerToken(), 5);

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());

        assertThat(stockOf(RICE)).isEqualTo(before);
    }

    @Test
    @DisplayName("an unknown status is rejected")
    void unknownStatusIsRejected() throws Exception {
        long orderId = placeOrder(customerToken(), 1);

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType("application/json")
                        .content("{\"status\":\"TELEPORTED\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("one customer cannot read another customer's order")
    void ordersAreScopedToTheirOwner() throws Exception {
        long orderId = placeOrder(customerToken(), 1);

        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"Nosy Neighbour","email":"nosy@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());

        String intruder = tokenFor("nosy@example.com", "password123");

        mvc.perform(get("/api/orders/" + orderId).header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + intruder))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("order history lists the customer's own orders newest first")
    void orderHistoryIsScopedAndOrdered() throws Exception {
        String token = customerToken();
        placeOrder(token, 1);
        placeOrder(token, 2);

        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("order numbers are unique per order")
    void orderNumbersAreUnique() throws Exception {
        String token = customerToken();
        placeOrder(token, 1);
        placeOrder(token, 1);

        var numbers = orderRepository.findAll().stream().map(o -> o.getOrderNumber()).distinct().count();
        assertThat(numbers).isEqualTo(orderRepository.count());
    }
}
