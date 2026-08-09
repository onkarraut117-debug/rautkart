package in.rautkart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The auth boundary: who can reach what. */
class AuthorizationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("catalogue browsing needs no token")
    void catalogueIsPublic() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        mvc.perform(get("/api/categories")).andExpect(status().isOk());
        mvc.perform(get("/api/products/featured")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("cart and orders reject anonymous callers")
    void privateEndpointsRequireAToken() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/addresses")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("a customer token cannot reach the admin API")
    void customerCannotReachAdminApi() throws Exception {
        String token = customerToken();

        mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a customer cannot mint a token through the admin login")
    void customerCannotUseAdminLogin() throws Exception {
        mvc.perform(post("/api/auth/admin/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"customer@rautkart.in","password":"customer123"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an admin token opens the admin API")
    void adminCanReachAdminApi() throws Exception {
        mvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProducts").isNumber());
    }

    @Test
    @DisplayName("a garbage token is rejected rather than trusted")
    void garbageTokenIsRejected() throws Exception {
        mvc.perform(get("/api/cart").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("signup always creates a CUSTOMER, never an admin")
    void signupCannotSelfAssignAdmin() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"Sneaky User","email":"sneaky@example.com",
                                 "password":"password123","role":"ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("duplicate email signup is refused")
    void duplicateEmailIsRefused() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"name":"Copycat","email":"customer@rautkart.in","password":"password123"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("wrong password does not leak whether the account exists")
    void wrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"customer@rautkart.in","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
