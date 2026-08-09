package in.rautkart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cart arithmetic and the checkout flow, including its effect on stock. */
class CartAndCheckoutTest extends AbstractIntegrationTest {

    private static final String RICE = "india-gate-basmati-rice";
    private static final String DAL = "toor-dal-arhar";

    private void addToCart(String token, String slug, int qty) throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"productId":%d,"quantity":%d}
                                """.formatted(productIdBySlug(slug), qty)))
                .andExpect(status().isOk());
    }

    private String checkoutBody(String paymentMethod) {
        return """
                {"paymentMethod":"%s",
                 "address":{"fullName":"Onkar Raut","phone":"9812345678",
                            "line1":"12 Shivaji Chowk","city":"Nashik",
                            "state":"Maharashtra","pincode":"422001"}}
                """.formatted(paymentMethod);
    }

    @Test
    @DisplayName("the server computes line totals, subtotal and the delivery fee")
    void cartTotalsAreComputedServerSide() throws Exception {
        String token = customerToken();
        addToCart(token, DAL, 1);   // 148.00

        mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1))
                .andExpect(jsonPath("$.subtotal").value(148.00))
                // Under the 500 threshold, so the 30 rupee fee applies.
                .andExpect(jsonPath("$.deliveryFee").value(30.00))
                .andExpect(jsonPath("$.total").value(178.00))
                .andExpect(jsonPath("$.amountForFreeDelivery").value(352.00));
    }

    @Test
    @DisplayName("crossing the threshold makes delivery free")
    void deliveryIsFreeOverTheThreshold() throws Exception {
        String token = customerToken();
        addToCart(token, RICE, 2);  // 378.00
        addToCart(token, DAL, 1);   // 148.00 -> 526.00

        mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(526.00))
                .andExpect(jsonPath("$.deliveryFee").value(0.00))
                .andExpect(jsonPath("$.total").value(526.00))
                .andExpect(jsonPath("$.amountForFreeDelivery").value(0.00));
    }

    @Test
    @DisplayName("adding the same product twice merges into one line")
    void repeatedAddsMerge() throws Exception {
        String token = customerToken();
        addToCart(token, DAL, 1);
        addToCart(token, DAL, 2);

        mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    @DisplayName("setting quantity to zero removes the line")
    void zeroQuantityRemovesTheLine() throws Exception {
        String token = customerToken();
        addToCart(token, DAL, 2);

        mvc.perform(put("/api/cart/items/" + productIdBySlug(DAL))
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @DisplayName("you cannot add more than the stock on hand")
    void cannotOverOrder() throws Exception {
        String token = customerToken();
        setStock(DAL, 5);

        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"productId":%d,"quantity":6}
                                """.formatted(productIdBySlug(DAL))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only 5 left in stock"));
    }

    @Test
    @DisplayName("checkout reserves stock, clears the cart and snapshots the address")
    void checkoutReservesStock() throws Exception {
        String token = customerToken();
        int before = stockOf(RICE);
        addToCart(token, RICE, 3);

        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(checkoutBody("COD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentRequired").value(false))
                .andExpect(jsonPath("$.order.status").value("PLACED"))
                .andExpect(jsonPath("$.order.paymentStatus").value("COD"))
                .andExpect(jsonPath("$.order.shipCity").value("Nashik"))
                .andExpect(jsonPath("$.order.orderNumber").exists());

        assertThat(stockOf(RICE)).isEqualTo(before - 3);

        mvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    @DisplayName("an address typed at checkout is kept in the address book")
    void checkoutSavesTheAddress() throws Exception {
        String token = customerToken();
        addToCart(token, DAL, 1);

        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(checkoutBody("COD")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/addresses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city").value("Nashik"))
                .andExpect(jsonPath("$[0].isDefault").value(true));
    }

    @Test
    @DisplayName("checking out an empty cart is refused")
    void emptyCartCannotCheckout() throws Exception {
        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken())
                        .contentType("application/json")
                        .content(checkoutBody("COD")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Your cart is empty"));
    }

    @Test
    @DisplayName("a malformed address is rejected field by field")
    void addressValidationReportsFields() throws Exception {
        String token = customerToken();
        addToCart(token, DAL, 1);

        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"paymentMethod":"COD",
                                 "address":{"fullName":"X","phone":"12","line1":"a",
                                            "city":"b","state":"c","pincode":"99"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['address.phone']").exists())
                .andExpect(jsonPath("$.fieldErrors['address.pincode']").exists());
    }
}
