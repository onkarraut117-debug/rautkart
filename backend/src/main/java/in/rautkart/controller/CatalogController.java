package in.rautkart.controller;

import in.rautkart.dto.CatalogDtos;
import in.rautkart.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Public storefront browsing. No authentication required. */
@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CatalogDtos.CategoryResponse> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/products")
    public CatalogDtos.PageResponse<CatalogDtos.ProductResponse> products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        return catalogService.searchProducts(q, category, minPrice, maxPrice, inStockOnly, sort, page, size);
    }

    @GetMapping("/products/featured")
    public List<CatalogDtos.ProductResponse> featured() {
        return catalogService.featured();
    }

    @GetMapping("/products/{slug}")
    public CatalogDtos.ProductResponse product(@PathVariable String slug) {
        return catalogService.getBySlug(slug);
    }
}
