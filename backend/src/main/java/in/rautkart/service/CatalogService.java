package in.rautkart.service;

import in.rautkart.dto.CatalogDtos;
import in.rautkart.entity.Category;
import in.rautkart.entity.Product;
import in.rautkart.exception.ApiException;
import in.rautkart.repository.CategoryRepository;
import in.rautkart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ---------------------------------------------------------------- storefront

    @Transactional(readOnly = true)
    public List<CatalogDtos.CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(Mappers::toCategory)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogDtos.PageResponse<CatalogDtos.ProductResponse> searchProducts(
            String q, String categorySlug, BigDecimal minPrice, BigDecimal maxPrice,
            boolean inStockOnly, String sort, int page, int size) {

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 60), sortOf(sort));

        Page<Product> result = productRepository.search(
                likePattern(q), blankToNull(categorySlug), minPrice, maxPrice, inStockOnly, pageable);

        return new CatalogDtos.PageResponse<>(
                result.getContent().stream().map(Mappers::toProduct).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public CatalogDtos.ProductResponse getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .filter(Product::isActive)
                .map(Mappers::toProduct)
                .orElseThrow(() -> ApiException.notFound("Product"));
    }

    @Transactional(readOnly = true)
    public List<CatalogDtos.ProductResponse> featured() {
        return productRepository.findTop8ByActiveTrueAndStockQtyGreaterThanOrderByCreatedAtDesc(0).stream()
                .map(Mappers::toProduct)
                .toList();
    }

    // -------------------------------------------------------------------- admin

    @Transactional(readOnly = true)
    public CatalogDtos.PageResponse<CatalogDtos.ProductResponse> listAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> result = productRepository.findAll(pageable);
        return new CatalogDtos.PageResponse<>(
                result.getContent().stream().map(Mappers::toProduct).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public CatalogDtos.ProductResponse createProduct(CatalogDtos.ProductRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> ApiException.badRequest("Unknown category"));

        Product product = Product.builder()
                .name(req.name().trim())
                .slug(uniqueSlug(req.name()))
                .description(req.description())
                .category(category)
                .price(req.price())
                .mrp(req.mrp())
                .unit(req.unit().trim())
                .stockQty(req.stockQty())
                .imageUrl(blankToNull(req.imageUrl()))
                .emoji(blankToNull(req.emoji()))
                .active(req.active() == null || req.active())
                .build();

        return Mappers.toProduct(productRepository.save(product));
    }

    @Transactional
    public CatalogDtos.ProductResponse updateProduct(Long id, CatalogDtos.ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product"));
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> ApiException.badRequest("Unknown category"));

        product.setName(req.name().trim());
        product.setDescription(req.description());
        product.setCategory(category);
        product.setPrice(req.price());
        product.setMrp(req.mrp());
        product.setUnit(req.unit().trim());
        product.setStockQty(req.stockQty());
        product.setImageUrl(blankToNull(req.imageUrl()));
        product.setEmoji(blankToNull(req.emoji()));
        if (req.active() != null) {
            product.setActive(req.active());
        }

        return Mappers.toProduct(productRepository.save(product));
    }

    /**
     * Soft delete. Products are referenced by historical orders, so we retire
     * them from the storefront instead of removing the row.
     */
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public CatalogDtos.CategoryResponse createCategory(CatalogDtos.CategoryRequest req) {
        String slug = Mappers.slugify(req.name());
        if (categoryRepository.findBySlug(slug).isPresent()) {
            throw ApiException.conflict("A category with that name already exists");
        }
        Category category = Category.builder()
                .name(req.name().trim())
                .slug(slug)
                .icon(req.icon())
                .sortOrder(req.sortOrder() == null ? 100 : req.sortOrder())
                .build();
        return Mappers.toCategory(categoryRepository.save(category));
    }

    // ------------------------------------------------------------------ helpers

    private Sort sortOf(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }

    private String uniqueSlug(String name) {
        String base = Mappers.slugify(name);
        String slug = base;
        int suffix = 2;
        while (productRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Builds a case-insensitive LIKE pattern; "%" when there is no search term. */
    private static String likePattern(String q) {
        String term = blankToNull(q);
        if (term == null) {
            return "%";
        }
        String escaped = term.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
    }
}
