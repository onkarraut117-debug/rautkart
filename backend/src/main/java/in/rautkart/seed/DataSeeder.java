package in.rautkart.seed;

import in.rautkart.entity.Category;
import in.rautkart.entity.Product;
import in.rautkart.entity.Role;
import in.rautkart.entity.User;
import in.rautkart.repository.CategoryRepository;
import in.rautkart.repository.ProductRepository;
import in.rautkart.repository.UserRepository;
import in.rautkart.service.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds demo data on first run. Safe to run repeatedly: every insert is guarded
 * by a lookup, so restarting the app never duplicates rows.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        Map<String, Category> categories = seedCategories();
        seedProducts(categories);
    }

    private void seedUsers() {
        createUserIfMissing("Store Admin", "admin@rautkart.in", "admin123", "9876543210", Role.ADMIN);
        createUserIfMissing("Demo Customer", "customer@rautkart.in", "customer123", "9812345678", Role.CUSTOMER);
    }

    private void createUserIfMissing(String name, String email, String password, String phone, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .phone(phone)
                .role(role)
                .build());
        log.info("Seeded {} account: {} / {}", role, email, password);
    }

    private Map<String, Category> seedCategories() {
        Object[][] rows = {
                {"Grains & Rice", "grains-rice", "🌾", 10},
                {"Dals & Pulses", "dals-pulses", "🫘", 20},
                {"Oils & Ghee", "oils-ghee", "🫒", 30},
                {"Spices & Masala", "spices-masala", "🌶️", 40},
                {"Dairy & Eggs", "dairy-eggs", "🥛", 50},
                {"Fruits & Vegetables", "fruits-vegetables", "🥕", 60},
                {"Snacks & Namkeen", "snacks-namkeen", "🍪", 70},
                {"Beverages", "beverages", "☕", 80},
                {"Bakery", "bakery", "🍞", 90},
                {"Household & Cleaning", "household-cleaning", "🧴", 100},
        };

        Map<String, Category> bySlug = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String slug = (String) row[1];
            Category category = categoryRepository.findBySlug(slug).orElseGet(() ->
                    categoryRepository.save(Category.builder()
                            .name((String) row[0])
                            .slug(slug)
                            .icon((String) row[2])
                            .sortOrder((Integer) row[3])
                            .build()));
            bySlug.put(slug, category);
        }
        return bySlug;
    }

    private void seedProducts(Map<String, Category> categories) {
        for (Object[] row : PRODUCTS) {
            String name = (String) row[0];
            String slug = Mappers.slugify(name);
            if (productRepository.existsBySlug(slug)) {
                continue;
            }
            productRepository.save(Product.builder()
                    .name(name)
                    .slug(slug)
                    .description((String) row[1])
                    .category(categories.get((String) row[2]))
                    .price(new BigDecimal((String) row[3]))
                    .mrp(new BigDecimal((String) row[4]))
                    .unit((String) row[5])
                    .stockQty((Integer) row[6])
                    .emoji((String) row[7])
                    .active(true)
                    .build());
        }
        log.info("Catalogue ready: {} products across {} categories",
                productRepository.count(), categoryRepository.count());
    }

    /** name, description, categorySlug, price, mrp, unit, stock, emoji */
    private static final List<Object[]> PRODUCTS = SeedCatalogue.rows();
}
