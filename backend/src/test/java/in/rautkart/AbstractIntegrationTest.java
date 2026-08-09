package in.rautkart;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.rautkart.repository.AddressRepository;
import in.rautkart.repository.CartItemRepository;
import in.rautkart.repository.OrderRepository;
import in.rautkart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared base for the integration tests. One PostgreSQL container is started for
 * the whole suite and reused, which keeps the run fast while still exercising
 * the real database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected AddressRepository addressRepository;

    /**
     * Orders and carts are cleared between tests so stock assertions start from a
     * known state. The seeded catalogue and demo users are left in place.
     */
    @BeforeEach
    void resetTransactionalState() {
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        addressRepository.deleteAll();
    }

    protected String tokenFor(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(body).get("token").asText();
    }

    protected String customerToken() throws Exception {
        return tokenFor("customer@rautkart.in", "customer123");
    }

    protected String adminToken() throws Exception {
        return tokenFor("admin@rautkart.in", "admin123");
    }

    protected Long productIdBySlug(String slug) {
        return productRepository.findBySlug(slug).orElseThrow().getId();
    }

    protected int stockOf(String slug) {
        return productRepository.findBySlug(slug).orElseThrow().getStockQty();
    }

    protected void setStock(String slug, int qty) {
        var product = productRepository.findBySlug(slug).orElseThrow();
        product.setStockQty(qty);
        productRepository.save(product);
    }
}
